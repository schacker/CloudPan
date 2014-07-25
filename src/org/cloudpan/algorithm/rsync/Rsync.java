package org.cloudpan.algorithm.rsync;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.cloudpan.algorithm.Adler32;
import org.cloudpan.algorithm.MD5;

/**
 *  Rsync算法
 */
public class Rsync {
	
	/**
	 *  根据补丁与文件名在服务器上生成一个新的文件.
	 * @param patch 补丁
	 * @param fileName 文件 
	 * @throws Exception 
	 * 思路：
	 */
	public static void createNewFile(Patch patch, String fileName) throws Exception{
		
		File file = new File(fileName);

		RandomAccessFile src = new RandomAccessFile(fileName,"r");//原始文件
		RandomAccessFile dst = new RandomAccessFile("temp_"+file.getName(),"rw");//打补丁之后的文件
		for(PatchPart part : patch.getPatchParts()){
			if(part instanceof PatchPartData){
				PatchPartData patchPartData =(PatchPartData)part;
				dst.write(patchPartData.getDatas());
			}else{
				PatchPartChunk patchPartChunk =(PatchPartChunk)part;
				src.seek(patchPartChunk.getIndex()*Constant.CHUNK_SIZE);
				byte[] buffer = new byte[patchPartChunk.getSize()];
				src.read(buffer);
				dst.write(buffer);
			}
		}

		File tempFile = new File("temp_"+file.getName());
		FileUtils.copyFile(tempFile,file);
		src.close();
		dst.close();
		FileUtils.deleteQuietly(tempFile);//删除temp文件
	}
	
	/**
	 * 根据文件，创建补丁
	 * @param checkSums 校验和
	 * @param fileName 文件名
	 * @param start 开始位置
	 * @param end 结束位置
	 * @return
	 * @throws Exception 
	 */
	public static Patch  createPatch(Map<Integer, List<Chunk>> checkSums, String fileName, long start, long end) throws Exception{
		Patch patch = new Patch();
		File file = new File(fileName);
		long length = file.length();
		if(start >= length){
			return patch;
		}
		//如果checkSums里面为空 直接生成patch传回去
		if(checkSums.isEmpty() || checkSums == null){
			PatchPartData  patchPartData = new PatchPartData();
			patchPartData.setDatas(readFile(fileName));
			patch.addPatchPart(patchPartData);
			return patch;
		}
		
		if(checkSums.isEmpty() || checkSums == null){
			PatchPartData  patchPartData = new PatchPartData();
			patchPartData.setDatas(readSize(fileName, start));
			patch.addPatchPart(patchPartData);
			start = start + 1024*512;
			return patch;
		}
		
		ArrayList<Byte> diffDatas = new ArrayList<Byte>();//存放不一致的数据
		while(start < length){
			byte[] buffer = readChunk(fileName,start); //读一块数据
			PatchPart  patchPart = matchCheckSums(checkSums, buffer);//是否匹配上
			if(patchPart != null){//匹配上
				if(diffDatas.size() > 0){ //有不一致的数据
					PatchPartData  patchPartData = new PatchPartData();
					byte[] temp = new byte[diffDatas.size()];
					for(int i  = 0; i < diffDatas.size(); i++){ //将不一致的数据放到temp数组
						temp[i] = diffDatas.get(i);
					}
					patchPartData.setDatas(temp);
					patch.addPatchPart(patchPartData);//先加不一致的数据
					diffDatas.clear(); //清空链表
				}
				patch.addPatchPart(patchPart);//再加匹配上的数据
				start = start+buffer.length;
				if(start >= length){
					return patch;
				}
				continue;
			}else{ //未匹配上
				start = start + 1;//右移一个字节
				if(start >= length){
					PatchPartData  patchPartData = new PatchPartData();
					byte[] temp = new byte[diffDatas.size()+buffer.length];
					//先把不一致的数据加入到temp
					for(int i = 0; i < diffDatas.size(); i++){
						temp[i] = diffDatas.get(i);
					}
					System.arraycopy(buffer, 0, temp, diffDatas.size(), buffer.length);
					patchPartData.setDatas(temp);
					patch.addPatchPart(patchPartData);
					return patch;
				}
				diffDatas.add(buffer[0]);
				continue;
			}
		}
		return patch;
	}
	public static Patch  createPatchAll(Map<Integer, List<Chunk>> checkSums, String fileName, long start, long end) throws Exception{
		Patch patch = new Patch();
		File file = new File(fileName);
		long length = file.length();
		if(start >= length){
			return patch;
		}
		if(checkSums.isEmpty() || checkSums == null){
			PatchPartData  patchPartData = new PatchPartData();
			patchPartData.setDatas(readSize(fileName, start));
			patch.addPatchPart(patchPartData);
			start = start + 1024*512;
			return patch;
		}
		return patch;
	}
	
	/**
	 * 匹配检验和
	 * @param checkSums
	 * @param buffer
	 * @return
	 */
	private static PatchPart matchCheckSums(Map<Integer,List<Chunk>> checkSums , byte[] buffer){
		int  weakCheckSum = calcWeakCheckSum(buffer); //计算弱校验和
		if(checkSums.containsKey(weakCheckSum)){ //判断是否包含弱校验和
			List<Chunk> strongCheckSums = checkSums.get(weakCheckSum);
			String strongCheckSum = calcStrongCheckSum(buffer);//计算强校验和
			for(Chunk chunk : strongCheckSums){
				if(strongCheckSum.equals(chunk.getStrongCheckSum())){
					PatchPartChunk  patchPartChunk = new PatchPartChunk();
					patchPartChunk.setIndex(chunk.getIndex());
					patchPartChunk.setSize(buffer.length);
					return patchPartChunk; //只有这一种情况会匹配上
				}
			}
		}
		return null;
	}
	
	/**
	 *  对文件分块，计算每块的弱校验和 与 强校验和.
	 * @param fileName 文件名
	 * @return 以弱校验和为key，强校验和组成的链表 为value的数据结构
	 */
	public static Map<Integer,List<Chunk>> calcCheckSum(String fileName){
		Map<Integer,List<Chunk>> checkSums = new HashMap<Integer,List<Chunk>>();
		File file = new File(fileName);
		if(!file.exists()){
			return null;//文件不存在直接返回null
		}
		FileInputStream fis = null;
		try {
			 fis = new FileInputStream(file);
			 byte[] buffer = new byte[Constant.CHUNK_SIZE];
			 int read = 0 ;
			 int index = 0 ;
			 while((read =fis.read(buffer)) != -1){
				 Chunk chunk  = new Chunk();
				 chunk.setIndex(index); //设置chunk的编号 从 0 开始
				 index++;
				 chunk.setSize(read); // 设置chunk的大小
				 int weakCheckSum = calcWeakCheckSum(buffer,read);//计算弱校验和
				 String strongCheckSum = calcStrongCheckSum(buffer,read);//计算强校验和
				 chunk.setWeakCheckSum(weakCheckSum);
				 chunk.setStrongCheckSum(strongCheckSum);
				 // 如果checkSums 包含有此弱校验和，那么把链表取出，将新的chunk加入链表
				if(checkSums.containsKey(weakCheckSum)){
					List<Chunk> chunks = checkSums.get(weakCheckSum);
					chunks.add(chunk);
				}else{
					List<Chunk> chunks = new ArrayList<Chunk>();
					chunks.add(chunk);
					checkSums.put(weakCheckSum,chunks);
				}
			 }
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return checkSums;
	}
	/**
	 * 根据文件名将文件读取到字节数组
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	private static byte[] readFile(String fileName) throws Exception{
		File file = new File(fileName);
		long len = file.length();
		if(file.length() > 1024*512){
			len = 1024*512;
		}
		byte[] buffer = new byte[(int)len];
		FileInputStream fis = new FileInputStream(fileName);
		fis.read(buffer);
		fis.close();
		return buffer;
	}
	/**
	 * 根据文件名和读取开始位置 读取固定长度（CHUNK_SIZE）的字节数组
	 * @param fileName
	 * @param start
	 * @return
	 * @throws Exception
	 */
	private static  byte[] readChunk(String fileName, long start) throws Exception{
		RandomAccessFile raf = new RandomAccessFile(new File(fileName), "rw");
		raf.seek(start);
		byte[] temp = new byte[Constant.CHUNK_SIZE];
		int read = raf.read(temp,0,Constant.CHUNK_SIZE);
		byte[] buffer = new byte[read];
		System.arraycopy(temp,0, buffer, 0,read);
		raf.close();
		return buffer;
	}
	
	private static  byte[] readSize(String fileName, long start) throws Exception{
		RandomAccessFile raf = new RandomAccessFile(new File(fileName), "rw");
		raf.seek(start);
		byte[] temp = new byte[1024*512];
		int read = raf.read(temp,0,1024*512);
		byte[] buffer = new byte[read];
		System.arraycopy(temp,0, buffer, 0,read);
		raf.close();
		return buffer;
	}
	

	/**
	 *  计算数据的Adler32
	 * @param datas 字节数组
	 * @param size 有效大小
	 * @return
	 */
	
	private static int calcWeakCheckSum(byte[] datas, int size){
		byte[] temp = new byte[size];
		for(int i = 0 ; i< size; i++){
			temp[i] = datas[i];
		}
		return Adler32.adler32(temp);
	}
	private static int calcWeakCheckSum(byte[] datas){
		return Adler32.adler32(datas);
	}
	/**
	 * 计算数据的MD5值
	 * @param datas 原始数据
	 * @param size  有效数据的大小
	 * @return
	 */
	private static String calcStrongCheckSum(byte[] datas,int size){
		byte[] temp = new byte[size];
		for(int i = 0 ; i< size; i++){
			temp[i] = datas[i];
		}
		return MD5.getMD5(temp);
	}
	private static String calcStrongCheckSum(byte[] datas){
		return MD5.getMD5(datas);
	}
}
