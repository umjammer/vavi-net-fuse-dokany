package vavi.net.fuse.dokany;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributes;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;

import dev.dokan.dokan_java.DokanFileSystemStub;
import dev.dokan.dokan_java.DokanOperations;
import dev.dokan.dokan_java.DokanUtils;
import dev.dokan.dokan_java.FileSystemInformation;
import dev.dokan.dokan_java.Unsigned;
import dev.dokan.dokan_java.constants.microsoft.CreateDisposition;
import dev.dokan.dokan_java.constants.microsoft.CreateOption;
import dev.dokan.dokan_java.constants.microsoft.FileSystemFlag;
import dev.dokan.dokan_java.constants.microsoft.NtStatuses;
import dev.dokan.dokan_java.masking.EnumInteger;
import dev.dokan.dokan_java.masking.MaskValueSet;
import dev.dokan.dokan_java.structure.ByHandleFileInformation;
import dev.dokan.dokan_java.structure.DokanFileInfo;
import dev.dokan.dokan_java.structure.DokanIOSecurityContext;


/**
 * This filesystem shows the content of a given directory and it sub directories
 */
public class JavaNioFileFS extends DokanFileSystemStub {

    /** */
    static final String ENV_NO_APPLE_DOUBLE = "no_apple_double";

    private final AtomicLong handleHandler;

    FileSystem fileSystem;

    static FileSystemInformation getFileSystemInfo(Map<String, Object> env) {
        MaskValueSet<FileSystemFlag> fsFeatures = MaskValueSet.of(FileSystemFlag.READ_ONLY_VOLUME, FileSystemFlag.CASE_PRESERVED_NAMES);
        return new FileSystemInformation(fsFeatures);
    }

    public JavaNioFileFS(FileSystem fileSystem, Map<String, Object> env) {
        super(getFileSystemInfo(env));
        this.fileSystem = fileSystem;
        this.handleHandler = new AtomicLong(0);
    }

    @Override
    public int zwCreateFile(WString rawPath, DokanIOSecurityContext securityContext, int rawDesiredAccess, int rawFileAttributes, int rawShareAccess, int rawCreateDisposition, int rawCreateOptions, DokanFileInfo dokanFileInfo) {
        Path p = getRootedPath(rawPath);

        //the files must exist and we are read only here
        CreateDisposition openOption = EnumInteger.enumFromInt(rawCreateDisposition, CreateDisposition.values());
        if (Files.exists(p)) {
            switch (openOption) {
                case FILE_CREATE:
                    return NtStatuses.STATUS_OBJECT_NAME_COLLISION;
                case FILE_OPEN:
                case FILE_OPEN_IF:
                    break;
                case FILE_OVERWRITE:
                case FILE_OVERWRITE_IF:
                case FILE_SUPERSEDE:
                    return NtStatuses.STATUS_ACCESS_DENIED;
                default:
                    return NtStatuses.STATUS_UNSUCCESSFUL;
            }
        } else {
            switch (openOption) {
                case FILE_CREATE:
                case FILE_OPEN_IF:
                case FILE_OVERWRITE_IF:
                case FILE_SUPERSEDE:
                    return NtStatuses.STATUS_ACCESS_DENIED;
                case FILE_OPEN:
                case FILE_OVERWRITE:
                    return NtStatuses.STATUS_OBJECT_NAME_NOT_FOUND;
                default:
                    return NtStatuses.STATUS_UNSUCCESSFUL;
            }

        }

        if (Files.isDirectory(p)) {
            if (MaskValueSet.maskValueSet(rawCreateOptions, CreateOption.values()).contains(CreateOption.FILE_NON_DIRECTORY_FILE)) {
                return NtStatuses.STATUS_FILE_IS_A_DIRECTORY;
            } else {
                dokanFileInfo.IsDirectory = 1;
            }
        }

        @Unsigned long val = this.handleHandler.incrementAndGet();
        if (val == 0) {
            val = this.handleHandler.incrementAndGet();
        }

        dokanFileInfo.Context = val;

        return NtStatuses.STATUS_SUCCESS;
    }

    @Override
    public void cleanup(WString rawPath, DokanFileInfo dokanFileInfo) {
        //nothing to do
    }

    @Override
    public void closeFile(WString rawPath, DokanFileInfo dokanFileInfo) {
        dokanFileInfo.Context = 0;
    }

    @Override
    public int getFileInformation(WString rawPath, ByHandleFileInformation handleFileInfo, DokanFileInfo dokanFileInfo) {
        Path p = getRootedPath(rawPath);
        if (dokanFileInfo.Context == 0) {
            return NtStatuses.STATUS_INVALID_HANDLE;
        }
        try {
            getFileInformation(p).copyTo(handleFileInfo);
            return NtStatuses.STATUS_SUCCESS;
        } catch (IOException e) {
            return NtStatuses.STATUS_IO_DEVICE_ERROR;
        }
    }

    private ByHandleFileInformation getFileInformation(Path p) throws IOException {
        DosFileAttributes attr = Files.readAttributes(p, DosFileAttributes.class);
        long index = 0;
        if (attr.fileKey() != null) {
            index = (long) attr.fileKey();
        }
        @Unsigned int fileAttr = 0;
        fileAttr |= attr.isArchive() ? WinNT.FILE_ATTRIBUTE_ARCHIVE : 0;
        fileAttr |= attr.isSystem() ? WinNT.FILE_ATTRIBUTE_SYSTEM : 0;
        fileAttr |= attr.isHidden() ? WinNT.FILE_ATTRIBUTE_HIDDEN : 0;
        fileAttr |= attr.isReadOnly() ? WinNT.FILE_ATTRIBUTE_READONLY : 0;
        fileAttr |= attr.isDirectory() ? WinNT.FILE_ATTRIBUTE_DIRECTORY : 0;
        fileAttr |= attr.isSymbolicLink() ? WinNT.FILE_ATTRIBUTE_REPARSE_POINT : 0;

        if (fileAttr == 0) {
            fileAttr |= WinNT.FILE_ATTRIBUTE_NORMAL;
        }

        return new ByHandleFileInformation(p.getFileName(), fileAttr, attr.creationTime(), attr.lastAccessTime(), attr.lastModifiedTime(), this.volumeSerialnumber, attr.size(), index);
    }

    @Override
    public int findFiles(WString rawPath, DokanOperations.FillWin32FindData rawFillFindData, DokanFileInfo dokanFileInfo) {
        Path path = getRootedPath(rawPath);
        if (dokanFileInfo.Context == 0) {
            return NtStatuses.STATUS_INVALID_HANDLE;
        }
        try (Stream<Path> stream = Files.list(path)) {
            stream.map(p -> {
                try {
                    return getFileInformation(path.resolve(p)).toWin32FindData();
                } catch (IOException e) {
                    return null;
                }
            }).forEach(file -> {
                if (file != null) {
                    rawFillFindData.fillWin32FindData(file, dokanFileInfo);
                }
            });
            return NtStatuses.STATUS_SUCCESS;
        } catch (IOException e) {
            return NtStatuses.STATUS_IO_DEVICE_ERROR;
        }
    }

    @Override
    public int getDiskFreeSpace(LongByReference freeBytesAvailable, LongByReference totalNumberOfBytes, LongByReference totalNumberOfFreeBytes, DokanFileInfo dokanFileInfo) {
        try {
            FileStore fileStore = fileSystem.getFileStores().iterator().next();
            freeBytesAvailable.setValue(fileStore.getUsableSpace());
            totalNumberOfBytes.setValue(fileStore.getTotalSpace());
            totalNumberOfFreeBytes.setValue(fileStore.getUnallocatedSpace());
            return NtStatuses.STATUS_SUCCESS;
        } catch (IOException e) {
            return NtStatuses.STATUS_IO_DEVICE_ERROR;
        }
    }

    @Override
    public int getVolumeInformation(Pointer rawVolumeNameBuffer, int rawVolumeNameSize, IntByReference rawVolumeSerialNumber, IntByReference rawMaximumComponentLength, IntByReference rawFileSystemFlags, Pointer rawFileSystemNameBuffer, int rawFileSystemNameSize, DokanFileInfo dokanFileInfo) {
        rawVolumeNameBuffer.setWideString(0L, DokanUtils.trimStrToSize(this.volumeName, rawVolumeNameSize));
        rawVolumeSerialNumber.setValue(this.volumeSerialnumber);
        rawMaximumComponentLength.setValue(this.fileSystemInformation.getMaxComponentLength());
        rawFileSystemFlags.setValue(this.fileSystemInformation.getFileSystemFeatures().intValue());
        rawFileSystemNameBuffer.setWideString(0L, DokanUtils.trimStrToSize(this.fileSystemInformation.getFileSystemName(), rawFileSystemNameSize));
        return NtStatuses.STATUS_SUCCESS;
    }

    private Path getRootedPath(WString rawPath) {
        String unixPath = rawPath.toString().replace('\\', '/');
        String relativeUnixPath = unixPath;
        if (unixPath.startsWith("/")) {
            // if it is already the root, we return the empty string
            relativeUnixPath = unixPath.length() == 1 ? "" : unixPath.substring(1);
        }
        return fileSystem.getPath(relativeUnixPath);
    }
}
