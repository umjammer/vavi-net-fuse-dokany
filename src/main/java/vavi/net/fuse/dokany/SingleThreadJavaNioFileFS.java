/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.fuse.dokany;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinBase.FILETIME;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;

import dev.dokan.dokan_java.structure.DokanFileInfo;
import dev.dokan.dokan_java.structure.DokanIOSecurityContext;


/**
 * SingleThreadJavaNioFileFS. (dokan)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/11/09 umjammer initial version <br>
 */
class SingleThreadJavaNioFileFS extends JavaNioFileFS {

    /** */
    private ExecutorService singleService = Executors.newSingleThreadExecutor();

    /** */
    private ExecutorService multiService = Executors.newCachedThreadPool();

    /**
     * @param fileSystem
     */
    public SingleThreadJavaNioFileFS(FileSystem fileSystem, Map<String, Object> env) throws IOException {
        super(fileSystem, env);
    }

    @Override
    public int zwCreateFile(WString rawPath,
                            DokanIOSecurityContext securityContext,
                            int rawDesiredAccess,
                            int rawFileAttributes,
                            int rawShareAccess,
                            int rawCreateDisposition,
                            int rawCreateOptions,
                            DokanFileInfo dokanFileInfo) {
        Future<Integer> f = singleService.submit(() -> {
            return super.zwCreateFile(rawPath, securityContext, rawDesiredAccess, rawFileAttributes, rawShareAccess, rawCreateDisposition, rawCreateOptions, dokanFileInfo);
        });
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int readFile(WString rawPath,
                        Pointer rawBuffer,
                        int rawBufferLength,
                        IntByReference rawReadLength,
                        long rawOffset,
                        DokanFileInfo dokanFileInfo) {
        Future<Integer> f = singleService.submit(() -> {
            return super.readFile(rawPath, rawBuffer, rawBufferLength, rawReadLength, rawOffset, dokanFileInfo);
        });
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int writeFile(WString rawPath,
                         Pointer rawBuffer,
                         int rawNumberOfBytesToWrite,
                         IntByReference rawNumberOfBytesWritten,
                         long rawOffset,
                         DokanFileInfo dokanFileInfo) {
        Future<Integer> f = singleService.submit(() -> {
            return super.writeFile(rawPath, rawBuffer, rawNumberOfBytesToWrite, rawNumberOfBytesWritten, rawOffset, dokanFileInfo);
        });
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void closeFile(WString rawPath, DokanFileInfo dokanFileInfo) {
        Future<Void> f = singleService.submit(() -> {
            super.closeFile(rawPath, dokanFileInfo);
            return null;
        });
        try {
            f.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int setFileAttributes(WString rawPath, int rawAttributes, DokanFileInfo dokanFileInfo) {
        Future<Integer> f = singleService.submit(() -> {
            return super.setFileAttributes(rawPath, rawAttributes, dokanFileInfo);
        });
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int setFileTime(WString rawPath,
                           FILETIME rawCreationTime,
                           FILETIME rawLastAccessTime,
                           FILETIME rawLastWriteTime,
                           DokanFileInfo dokanFileInfo) {
        Future<Integer> f = singleService.submit(() -> {
            return super.setFileTime(rawPath, rawCreationTime, rawLastAccessTime, rawLastWriteTime, dokanFileInfo);
        });
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int deleteDirectory(WString rawPath, DokanFileInfo dokanFileInfo) {
        Future<Integer> f = singleService.submit(() -> {
            return super.deleteDirectory(rawPath, dokanFileInfo);
        });
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int moveFile(WString rawPath, WString rawNewFileName, boolean rawReplaceIfExisting, DokanFileInfo dokanFileInfo) {
        Future<Integer> f = singleService.submit(() -> {
            return super.moveFile(rawPath, rawNewFileName, rawReplaceIfExisting, dokanFileInfo);
        });
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int deleteFile(WString rawPath, DokanFileInfo dokanFileInfo) {
        Future<Integer> f = singleService.submit(() -> {
            return super.deleteFile(rawPath, dokanFileInfo);
        });
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int getDiskFreeSpace(LongByReference freeBytesAvailable,
                                LongByReference totalNumberOfBytes,
                                LongByReference totalNumberOfFreeBytes,
                                DokanFileInfo dokanFileInfo) {
        Future<Integer> f = multiService.submit(() -> {
            return super.getDiskFreeSpace(freeBytesAvailable,
                                          totalNumberOfBytes,
                                          totalNumberOfFreeBytes,
                                          dokanFileInfo);
        });
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int getVolumeInformation(Pointer rawVolumeNameBuffer,
                                    int rawVolumeNameSize,
                                    IntByReference rawVolumeSerialNumber,
                                    IntByReference rawMaximumComponentLength,
                                    IntByReference rawFileSystemFlags,
                                    Pointer rawFileSystemNameBuffer,
                                    int rawFileSystemNameSize,
                                    DokanFileInfo dokanFileInfo) {
        Future<Integer> f = multiService.submit(() -> {
            return super.getVolumeInformation(rawVolumeNameBuffer,
                                              rawVolumeNameSize,
                                              rawVolumeSerialNumber,
                                              rawMaximumComponentLength,
                                              rawFileSystemFlags,
                                              rawFileSystemNameBuffer,
                                              rawFileSystemNameSize,
                                              dokanFileInfo);
        });
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }
}
