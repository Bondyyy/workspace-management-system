package com.wms.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaQRUtilTest {

    @Test
    void taoAnhPngReturnsBytesForContent() {
        byte[] png = MaQRUtil.taoAnhPng("WMS-TEST-QR", 120);

        assertThat(png).isNotEmpty();
    }

    @Test
    void taoDataUriPngReturnsPngDataUri() {
        String dataUri = MaQRUtil.taoDataUriPng("WMS-TEST-QR", 120);

        assertThat(dataUri).startsWith("data:image/png;base64,");
    }

    @Test
    void blankContentKeepsCurrentEmptyBehavior() {
        assertThat(MaQRUtil.taoAnhPng(null)).isEmpty();
        assertThat(MaQRUtil.taoAnhPng("   ")).isEmpty();
        assertThat(MaQRUtil.taoDataUriPng("", 120)).isEmpty();
    }
}
