package org.cloudpan.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 */
public class CloudPanServerMatchFileUtil {
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private String filePath;
	
	public CloudPanServerMatchFileUtil(
			String filePath){
		this.filePath = filePath;
	}
	@SuppressWarnings("unchecked")
	public Map<String,Long>  parseMatchFile() throws Exception{
		File file = new File(filePath);
		if(file.exists()){
			ois = new ObjectInputStream(new FileInputStream(file));
			Map<String,Long> matchDatas =  (Map<String, Long>) ois.readObject();
			ois.close();
			return matchDatas;
		}else{
			return new ConcurrentHashMap<String, Long>();
		}
	}
	
	public void saveMatchFile(Map<String, Long> matchDatas) throws Exception{
		File file =new File(filePath);
		if( !file.exists() ){
			File parent = file.getParentFile();
			if( !parent.exists()){
				parent.mkdirs();
			}
			file.createNewFile();
		}
		oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(matchDatas);
		oos.close();
	}
}
