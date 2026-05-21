package com.wms.service.TrangChuGioiThieu;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NguoiDungInputValidatorTest {

    @Test
    void registrationRejectsBlankUsername() {
        assertThat(NguoiDungInputValidator.isRegistrationValid("", "Nguyen Van A", "a@example.com", "secret123")).isFalse();
        assertThat(NguoiDungInputValidator.isRegistrationValid(null, "Nguyen Van A", "a@example.com", "secret123")).isFalse();
        assertThat(NguoiDungInputValidator.isRegistrationValid("   ", "Nguyen Van A", "a@example.com", "secret123")).isFalse();
    }

    @Test
    void registrationRejectsBlankEmail() {
        assertThat(NguoiDungInputValidator.isRegistrationValid("user01", "Nguyen Van A", "", "secret123")).isFalse();
        assertThat(NguoiDungInputValidator.isRegistrationValid("user01", "Nguyen Van A", null, "secret123")).isFalse();
        assertThat(NguoiDungInputValidator.isRegistrationValid("user01", "Nguyen Van A", "   ", "secret123")).isFalse();
    }

    @Test
    void registrationRejectsBlankPassword() {
        assertThat(NguoiDungInputValidator.isRegistrationValid("user01", "Nguyen Van A", "a@example.com", "")).isFalse();
        assertThat(NguoiDungInputValidator.isRegistrationValid("user01", "Nguyen Van A", "a@example.com", null)).isFalse();
        assertThat(NguoiDungInputValidator.isRegistrationValid("user01", "Nguyen Van A", "a@example.com", "   ")).isFalse();
    }

    @Test
    void registrationRejectsInvalidEmailFormat() {
        assertThat(NguoiDungInputValidator.isRegistrationValid("user01", "Nguyen Van A", "not-an-email", "secret123")).isFalse();
    }

    @Test
    void registrationAcceptsValidInput() {
        assertThat(NguoiDungInputValidator.isRegistrationValid("user01", "Nguyen Van A", "a@example.com", "secret123")).isTrue();
    }
}
