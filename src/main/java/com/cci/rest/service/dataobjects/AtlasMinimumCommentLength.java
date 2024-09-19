package com.cci.rest.service.dataobjects;

public class AtlasMinimumCommentLength {
	
	int commentLength;

	public int getCommentLength() {
		return commentLength;
	}

	public void setCommentLength(int commentLength) {
		this.commentLength = commentLength;
	}

	@Override
	public String toString() {
		return "AtlasMinimunCommentLength [commentLength=" + commentLength + "]";
	}

}
