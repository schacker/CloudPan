package org.cloudpan.algorithm.rsync;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 补丁
 */
public class Patch implements Serializable{
	private static final long serialVersionUID = 1L;
	private ArrayList<PatchPart> patchParts = new ArrayList<PatchPart>();
	/**
	 * 向patchParts中添加PatchPart
	 * @param patchPart
	 */
	public void addPatchPart(PatchPart patchPart){
		this.patchParts.add(patchPart);
	}
	public ArrayList<PatchPart> getPatchParts() {
		return patchParts;
	}

	
	public void setPatchParts(ArrayList<PatchPart> patchParts) {
		this.patchParts = patchParts;
	}
	
	
}
