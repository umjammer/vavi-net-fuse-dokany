/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.fuse.dokany;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Paths;
import java.util.Map;

import vavi.net.fuse.Fuse;
import vavi.util.Debug;

import dev.dokan.dokan_java.DokanFileSystemStub;
import dev.dokan.dokan_java.constants.dokany.MountOption;
import dev.dokan.dokan_java.masking.MaskValueSet;


/**
 * DokanyFuse. (dokany engine)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/11/09 umjammer initial version <br>
 */
public class DokanyFuse implements Fuse {

    /** */
    public static final String ENV_NO_APPLE_DOUBLE = JavaNioFileFS.ENV_NO_APPLE_DOUBLE;

    /** */
    private DokanFileSystemStub fuse;

    @Override
    public void mount(FileSystem fs, String mountPoint, Map<String, Object> env) throws IOException {
        if (env.containsKey(ENV_SINGLE_THREAD) && Boolean.class.cast(env.get(ENV_SINGLE_THREAD))) {
            fuse = new SingleThreadJavaNioFileFS(fs, env);
Debug.println("use single thread");
        } else {
            fuse = new JavaNioFileFS(fs, env);
        }
        // TODO select option
        MaskValueSet<MountOption> mountOptions = MaskValueSet.of(MountOption.STD_ERR_OUTPUT, MountOption.WRITE_PROTECTION, MountOption.CURRENT_SESSION);

        fuse.mount(Paths.get(mountPoint), mountOptions);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { try { close(); } catch (Exception e) { e.printStackTrace(); }}));
    }

    @Override
    public void close() throws IOException {
        if (fuse != null) {
Debug.println("unmount...");
            fuse.unmount();
            fuse = null;
Debug.println("unmount done");
        }
    }
}

/* */
