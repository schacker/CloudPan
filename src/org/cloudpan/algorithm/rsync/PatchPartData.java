package org.cloudpan.algorithm.rsync;
/**
 * 特殊的补丁块，里面存放的是与服务器不一致的数据.
 */
public class PatchPartData extends PatchPart{
	private static final long serialVersionUID = 1L;
	private byte[] datas;
	private int length;
	public byte[] getDatas() {
		return datas;
	}
	public void setDatas(byte[] datas) {
		this.datas = datas;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
}
