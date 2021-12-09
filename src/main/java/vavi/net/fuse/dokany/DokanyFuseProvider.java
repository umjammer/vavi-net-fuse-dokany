/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.fuse.dokany;

import vavi.net.fuse.Fuse;
import vavi.net.fuse.FuseProvider;


/**
 * DokanyFuseProvider.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/11/09 umjammer initial version <br>
 */
public class DokanyFuseProvider implements FuseProvider {

    @Override
    public Fuse getFuse() {
        return new DokanyFuse();
    }
}

/* */
