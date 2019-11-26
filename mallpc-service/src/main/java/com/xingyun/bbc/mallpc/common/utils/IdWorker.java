package com.xingyun.bbc.mallpc.common.utils;

import org.apache.commons.lang.math.RandomUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;

/***
 * 
* @file: com.xingyun.xybb.purchase.utils.IdWorker.java  
* @Description: [id生成器:参照雪花算法改造;id业务参数之间全局唯一,且为全局增长趋势] 
* @author: hmh 
* @version: v1.0
* @date: 2019年7月8日 下午8:56:36
* @Company: www.xyb2b.com
 */
public class IdWorker {
	protected static IdWorker idWorker=new IdWorker();
//	protected SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmssSSS");
	protected SimpleDateFormat sdf=new SimpleDateFormat("SSSss");

	/** 毫秒内序列(0~4095) */
    private long sequence = 0L;
 
    /** 上次生成ID的时间截 */
    private long lastTimestamp = -1L;
    
    /** 序列在id中占的位数 */
    private final long sequenceBits = 12L;
    /** 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095) */
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);
    
    private String netSign=getNetSign();
    
    private IdWorker(){
    }
    
    protected static IdWorker getInstance(){
	   return idWorker;
   }
    
    /**
     * 获得下一个ID (该方法是线程安全的)
     * @return SnowflakeId
     */
    private synchronized String nextId(String biz) {
        long timestamp = timeGen();
 
        //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
 
        //如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            //毫秒内序列溢出
            if (sequence == 0) {
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        //时间戳改变，毫秒内序列重置
        else {
            sequence = generateRandom();
        }
        
        //上次生成ID的时间截
        lastTimestamp = timestamp;
        
        //这里为了可读性简单组合
        StringBuffer sb =new StringBuffer();
        sb.append(biz).append(sdf.format(timestamp)).append(netSign).append(sequence);

        return  sb.toString();
    }
 
    
    /***
     * 
     * @Description: [获取机器网络参数]
     * @param: @return   
     * @return: String  
     * @author: hmh
     * @version: v1.0
     * @date: 2019年7月9日 上午1:01:29
     */
    private String getNetSign(){
    	StringBuffer sb =new StringBuffer();
    	try {
			String hostAddress = InetAddress.getLocalHost().getHostAddress();
            Integer sign=Integer.parseInt(hostAddress.substring(hostAddress.lastIndexOf(".")+1, hostAddress.length()));
            if(0<=sign && sign<10){
                sb.append("00").append(sign).toString();
            }
            else if(10<=sign && sign <100){
                 sb.append("0").append(sign).toString();
            }
            else{
            	 sb.append(sign).toString();
            }
		} catch (UnknownHostException e) {
			 sb.append(RandomUtils.nextInt(9)).append(RandomUtils.nextInt(9)).append(RandomUtils.nextInt(9)).toString();
		}
    	System.out.println("机器网络参数："+sb);
    	return sb.toString();
    }
    
    
    //生成的ID，例如message-id/ order-id/ tiezi-id，在数据量大时往往需要分库分表，这些ID经常作为取模分库分表的依据，为了分库分表后数据均匀，ID生成往往有“取模随机性”的需求，所以我们通常把每秒内的序列号放在ID的最末位，保证生成的ID是随机的。
    //又如果，我们在跨毫秒时，序列号总是归0，会使得序列号为0的ID比较多，导致生成的ID取模后不均匀。解决方法是，序列号不是每次都归0，而是归一个0到9的随机数，这个地方。
    protected long generateRandom() {
        return RandomUtils.nextInt(9);
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }
 
    /**
     * 返回以毫秒为单位的当前时间
     * @return 当前时间(毫秒)
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }
    
    /**
     * 
     * @Description: [生成对应业务参数的下个id，id 保持业务间都是全局唯一]
     * @param: @param biz 业务参数，ID前缀
     * @param: @return   
     * @return: String  
     * @author: hmh
     * @version: v1.0
     * @date: 2019年7月9日 上午12:43:36
     */
    public static String getNextId(String biz)
    {
    	return IdWorker.getInstance().nextId(biz);
    }    
    
   /* public static void main(String[] args) {
    	Map map=new LinkedHashMap<String, Integer>();
		for(int i=0;i<10000;i++){
			map.put(IdWorker.getNextId("XD"), 1);
			map.put(IdWorker.getNextId("XD"), 2);
			map.put(IdWorker.getNextId("XD"), 3);
			map.put(IdWorker.getNextId("XD"), 4);
			map.put(IdWorker.getNextId("XD"), 5);
			map.put(IdWorker.getNextId("XD"), 6);
			map.put(IdWorker.getNextId("XD"), 7);
			map.put(IdWorker.getNextId("XD"), 8);
			map.put(IdWorker.getNextId("XD"), 9);
			map.put(IdWorker.getNextId("XD"), 10);
		}
		map.forEach((key,value)->{
			System.out.println(key);
		});
		System.out.println("map.size :"+map.size());
	}*/

}
