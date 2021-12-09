/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.nio.file.FileSystem;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import vavi.net.fuse.Base;
import vavi.net.fuse.Fuse;
import vavi.util.Debug;


/**
 * Test4. (jimfs, fuse)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/11/09 umjammer initial version <br>
 */
@EnabledOnOs(OS.WINDOWS)
@DisabledIfEnvironmentVariable(named = "GITHUB_WORKFLOW", matches = ".*")
public class Main4 {

    static {
        System.setProperty("vavi.util.logging.VaviFormatter.extraClassMethod", "co\\.paralleluniverse\\.fuse\\.LoggedFuseFilesystem#log");
    }

    FileSystem fs;
    String mountPoint;
    Map<String, Object> options;

    @BeforeEach
    public void before() throws Exception {

        mountPoint = System.getenv("TEST4_MOUNT_POINT");
Debug.println("mountPoint: " + mountPoint);

        fs = Jimfs.newFileSystem(Configuration.unix());

        options = new HashMap<>();
        options.put("fsname", "jimfs_fs" + "@" + System.currentTimeMillis());
        options.put("noappledouble", null);
        //options.put("noapplexattr", null);
    }

    @Test
    public void test01() throws Exception {
        String providerClassName = "vavi.net.fuse.fusejna.FuseJnaFuseProvider";
        System.setProperty("vavi.net.fuse.FuseProvider.class", providerClassName);
System.err.println("--------------------------- " + providerClassName + " ---------------------------");

        Base.testFuse(fs, mountPoint, options);

        fs.close();
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        Main4 app = new Main4();
        app.before();

        Fuse.getFuse().mount(app.fs, app.mountPoint, app.options);

        CountDownLatch cdl = new CountDownLatch(1);
        cdl.await();
    }
}

/* */
