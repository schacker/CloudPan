package org.cloudpan.algorithm;
/**
 * Adler32算法实现
 * 
 * A = 1 + D1 + D2 + ... + Dn (mod 65521)
 * B = (1 + D1) + (1 + D1 + D2) + ... + (1 + D1 + D2 + ... + Dn) (mod 65521)
    = n×D1 + (n−1)×D2 + (n−2)×D3 + ... + Dn + n (mod 65521)
 *	Adler-32(D) = B × 65536 + A
 * 
 */
public class Adler32 {
	public static final int MOD= 65521;
	
	public static int adler32(byte[] datas){
		int A = 1;
		int B = 0;
		for(byte b : datas){
			A = A + b;
			B = A + B; 
		}
		A = A%MOD;
		B = B%MOD;
		return (B<<16)|A; //左移十六位再加上A
	}
	
	public static int nextAdler32(int adler32 , byte preByte , byte nextByte , int length){
		// 1. 计算出原来的A 和 B
		int oldA = adler32&0xFFFF;
		int oldB = (adler32 >>16)&0xFFFF;
		//2. 计算新的A 和 B
		int A  = oldA - preByte + nextByte;
		int B  = A +oldB - length*preByte  -1;
		//3. 得到新的Adler32
		return (B%MOD)<<16|(A%MOD);
	}
}
