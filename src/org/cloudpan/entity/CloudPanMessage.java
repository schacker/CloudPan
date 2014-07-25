package org.cloudpan.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.cloudpan.algorithm.rsync.Chunk;
import org.cloudpan.algorithm.rsync.Patch;

/**
 *  系统消息定义
 */
public class CloudPanMessage implements Serializable{
	private static final long serialVersionUID = 1L;
	//用户管理
	private int msgType;//消息类型
	private Account acct;//账户
	private String content;//消息内容
	
	//文件同步
	private String fileName; //文件名称
	private String sourceFileName;//本地的文件名称
	private long   fileLength; //文件大小
	private Map<String,Long> matchFile;//匹配的文件
	private Map<Integer,List<Chunk>> checkSums; //校验和
	private Patch patch; //补丁
	
	public int getMsgType() {
		return msgType;
	}
	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}
	public Account getAcct() {
		return acct;
	}
	public void setAcct(Account acct) {
		this.acct = acct;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public long getFileLength() {
		return fileLength;
	}
	public void setFileLength(long fileLength) {
		this.fileLength = fileLength;
	}
	public Map<Integer, List<Chunk>> getCheckSums() {
		return checkSums;
	}
	public void setCheckSums(Map<Integer, List<Chunk>> checkSums) {
		this.checkSums = checkSums;
	}
	public Patch getPatch() {
		return patch;
	}
	public void setPatch(Patch patch) {
		this.patch = patch;
	}
	public Map<String, Long> getMatchFile() {
		return matchFile;
	}
	public void setMatchFile(Map<String, Long> matchFile) {
		this.matchFile = matchFile;
	}
	public String getSourceFileName() {
		return sourceFileName;
	}
	public void setSourceFileName(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}
	
}
