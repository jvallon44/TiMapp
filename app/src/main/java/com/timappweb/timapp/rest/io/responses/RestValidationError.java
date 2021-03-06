package com.timappweb.timapp.rest.io.responses;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by stephane on 9/12/2015.
 */
public class RestValidationError extends RestResponse {

    @Expose
    @SerializedName("returnCode")
    public int code;

    @Expose
    @SerializedName("message")
    public String message;

    @Expose
    @SerializedName("validationErrors")
    public JsonObject errors;

    // =============================================================================================

    private ServerValidationParser parsedErrors;

    // =============================================================================================

    public String getMessage() {
        return message;
    }
    public String getFullMessage() {
        return (message != null ? message : "") + (hasError("Places.Quota") ? getError("Places.Quota") : "");
    }
    public boolean hasFullMessage() {
        return getFullMessage() != "";
    }

    public boolean hasMessage() {
        return message != null && message.length() > 0;
    }

    public String toString(){
        return "RestValidationError[code: "+code+"; message=" + message + ";]";
    }

    public ServerValidationParser getErrors(){
        if (this.parsedErrors == null){
            this.parsedErrors = new ServerValidationParser(this.errors);
        }
        return this.parsedErrors;
    }

    public String getError(String path){
        return this.getErrors().getMessage(path);
    }

    public boolean hasError(String path){
        return this.getErrors().has(path);
    }
}
