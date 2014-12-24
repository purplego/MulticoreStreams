package skyline.algorithms;

import skyline.model.SkyTuple;
/**
 * @author Purple Wang
 * Jan 20, 2014
 */
public class IsDominate {
	
	
	/**
	 * dominateBetweenTuples�������Ƚ�����Tuple����֮���֧���ϵ
	 * @param t1
	 * @param t2
	 * @return ����ֵΪ0��x֧��y��1��y֧��x��2��x��y����֧��
	 */
	public static int dominateBetweenTuples(SkyTuple t1, SkyTuple t2){
		
		double[] x = t1.getAttrs();
		double[] y = t2.getAttrs();
		int dim = x.length;
		int isDominate = dominate(dim, x, y);
		return isDominate;
		
	}

	/**
	 * dominate�������ж�����dά����Ԫ��x��y֮���֧���ϵ
	 * @param dim ����Ƚϵ�Ԫ���ά��
	 * @param x ����Ƚϵĵ�һ��Ԫ��
	 * @param y ����Ƚϵĵڶ���Ԫ��
	 * @return ����ֵΪ0��x֧��y��1��y֧��x��2��x��y����֧��
	 */
	//����Ԫ���ά����ֵ֮��Ĺ�ϵ��the smaller the better
	public static int dominate(int dim,double[]x,double[]y){
		
		int flagx=0;
		int flagy=0;
		int i = 0;
		for(i = 0;i < dim;i++){
			if(x[i] < y[i])
				flagx = 1;
			else if(x[i] > y[i])
				flagy = 1;
			//��ǰ����֧���ϵ�Ƚ�
			if(flagx==1 && flagy==1)
				return 2;
		}
		if(flagx == 0 && flagy == 0)
			return 2;						//��ʾx=y������߶���������
		if(flagx == 1 && flagy == 0)
			return 0;						//��ʾx֧��y
		else if(flagx == 0 && flagy == 1)
			return 1;						//��ʾy֧��x
		else if(flagx == 1 && flagy == 1)
			return 2;						//��ʾx��y����֧��
		return -1;
	}
	
	/**
	 * ***************************************************************************
	 */
	/**
	 * dominateBetweenTuples�������Ƚ�����Tuple����t1��t2֮���֧���ϵ
	 * @param t1 ��1��Ԫ��;
	 * @param t2 ��2��Ԫ��;
	 * @return ����ֵΪtrue��x֧��y, false��ʾx��֧��y;
	 */
	public static boolean dominate(SkyTuple t1, SkyTuple t2){
		
		double[] x = t1.getAttrs();
		double[] y = t2.getAttrs();
		//int dim = x.length;
		boolean isDominate = dominate(x, y);
		return isDominate;
	}

	/**
	 * dominate�������ж�����dά����Ԫ��x��y֮���֧���ϵ
	 * @param x ����Ƚϵĵ�һ��Ԫ��
	 * @param y ����Ƚϵĵڶ���Ԫ��
	 * @return ����ֵΪtrue��x֧��y, false��ʾx��֧��y;
	 */
	//����Ԫ���ά����ֵ֮��Ĺ�ϵ��the smaller the better
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
