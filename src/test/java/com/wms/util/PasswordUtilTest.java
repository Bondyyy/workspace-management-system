package com.wms.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordUtilTest {

    @Test
    void hashReturnsNonBlankValue() {
        String hashed = PasswordUtil.hash("secret123");

        assertThat(hashed).isNotBlank();
    }

    @Test
    void verifyReturnsTrueForCorrectPassword() {
        String hashed = PasswordUtil.hash("secret123");

        assertThat(PasswordUtil.verify("secret123", hashed)).isTrue();
    }

    @Test
    void verifyReturnsFalseForWrongPassword() {
        String hashed = PasswordUtil.hash("secret123");

        assertThat(PasswordUtil.verify("wrong-password", hashed)).isFalse();
    }

    @Test
    void hashUsesSaltSoSamePasswordGetsDifferentHashes() {
        String firstHash = PasswordUtil.hash("secret123");
        String secondHash = PasswordUtil.hash("secret123");

        assertThat(firstHash).isNotEqualTo(secondHash);
    }
}
