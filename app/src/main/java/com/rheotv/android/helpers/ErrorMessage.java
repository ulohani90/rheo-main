package com.rheotv.android.helpers;

public final class ErrorMessage {



    public static final ErrorMessageItem SERVER_RESPONSE_NULL = new ErrorMessageItem("server_response_null",
                "Oops, something went wrong. Please try again");

    public static final ErrorMessageItem DEFAULT = new ErrorMessageItem("default",
            "Oops, something went wrong. Please try again");
    public static class ErrorMessageItem{
        String code;
        String message;
        public ErrorMessageItem(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
