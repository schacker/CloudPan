package org.cloudpan.algorithm.rsync;
/**
 * 特殊的补丁块，里面存放的是与服务器一致的数据的编号
 * 所以只需要存放编号
 */
public class PatchPartChunk extends PatchPart{
	private static final long serialVersionUID = 1L;
	private int index; //编号
	private int size; //大小
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
}
