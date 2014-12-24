package skyline.algorithms;

import skyline.model.SkyTuple;
/**
 * @author Purple Wang
 * Jan 20, 2014
 */
public class IsDominate {
	
	
	/**
	 * dominateBetweenTuples方法，比较两个Tuple对象之间的支配关系
	 * @param t1
	 * @param t2
	 * @return 返回值为0：x支配y；1：y支配x；2：x与y互不支配
	 */
	public static int dominateBetweenTuples(SkyTuple t1, SkyTuple t2){
		
		double[] x = t1.getAttrs();
		double[] y = t2.getAttrs();
		int dim = x.length;
		int isDominate = dominate(dim, x, y);
		return isDominate;
		
	}

	/**
	 * dominate方法，判断两个d维数据元组x和y之间的支配关系
	 * @param dim 参与比较的元组的维数
	 * @param x 参与比较的第一个元组
	 * @param y 参与比较的第二个元组
	 * @return 返回值为0：x支配y；1：y支配x；2：x与y互不支配
	 */
	//假设元组各维属性值之间的关系是the smaller the better
	public static int dominate(int dim,double[]x,double[]y){
		
		int flagx=0;
		int flagy=0;
		int i = 0;
		for(i = 0;i < dim;i++){
			if(x[i] < y[i])
				flagx = 1;
			else if(x[i] > y[i])
				flagy = 1;
			//提前结束支配关系比较
			if(flagx==1 && flagy==1)
				return 2;
		}
		if(flagx == 0 && flagy == 0)
			return 2;						//表示x=y，则二者都保存下来
		if(flagx == 1 && flagy == 0)
			return 0;						//表示x支配y
		else if(flagx == 0 && flagy == 1)
			return 1;						//表示y支配x
		else if(flagx == 1 && flagy == 1)
			return 2;						//表示x和y互不支配
		return -1;
	}
	
	/**
	 * ***************************************************************************
	 */
	/**
	 * dominateBetweenTuples方法，比较两个Tuple对象t1与t2之间的支配关系
	 * @param t1 第1个元组;
	 * @param t2 第2个元组;
	 * @return 返回值为true：x支配y, false表示x不支配y;
	 */
	public static boolean dominate(SkyTuple t1, SkyTuple t2){
		
		double[] x = t1.getAttrs();
		double[] y = t2.getAttrs();
		//int dim = x.length;
		boolean isDominate = dominate(x, y);
		return isDominate;
	}

	/**
	 * dominate方法，判断两个d维数据元组x和y之间的支配关系
	 * @param x 参与比较的第一个元组
	 * @param y 参与比较的第二个元组
	 * @return 返回值为true：x支配y, false表示x不支配y;
	 */
	//假设元组各维属性值之间的关系是the smaller the better
	private static boolean dominate(double[] x, double[] y) {
		if (x.length != y.length) {
			System.out.println("The Two datum ara not identical!");
			return false;
		}
		int flags = 0;
		for (int i = 0; i < x.length; i++) {
			if (x[i] < y[i])
				flags = 1;
			else if (y[i] < x[i])
				return false;
		}
		if (flags == 1)
			return true;
		return false;
	}
	
}
