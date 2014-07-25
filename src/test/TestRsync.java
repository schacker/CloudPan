package test;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.cloudpan.algorithm.rsync.Chunk;
import org.cloudpan.algorithm.rsync.Patch;
import org.cloudpan.algorithm.rsync.Rsync;

public class TestRsync {
	public static void main(String[] args) throws Exception{
		long start = System.currentTimeMillis();
		//1. 算出校验和
		Map<Integer,List<Chunk>> checkSums = Rsync.calcCheckSum("F:\\local\\weather.txt");
		//2. 生成补丁
		File file = new File("F:\\local\\weather.txt");
		Patch patch = Rsync.createPatch(checkSums, "F:\\local\\weather.txt",0,file.length());
		//3. 生成新的文件
		Rsync.createNewFile(patch, "F:\\remote\\weather.txt");
		long end = System.currentTimeMillis();
		System.out.println("运行时间为 :"+(end-start));
	
	}
}
