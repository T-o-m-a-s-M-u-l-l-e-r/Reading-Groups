package com.muller_tomas.reading_groups.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidGroupJoinLinkException extends RuntimeException {
	public InvalidGroupJoinLinkException() {
		super("Group join link is invalid or expired");
	}
}
