package com.amr.book.exception;

public class OperationNotPermittedException extends RuntimeException {

    public OperationNotPermittedException(String msg) {
        //to call the constructor of the parent class which is RuntimeException
        super(msg);
    }
}
