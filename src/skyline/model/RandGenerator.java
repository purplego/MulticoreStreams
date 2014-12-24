package skyline.model;
/**
 * @author Purple Wang
 * Jan 20, 2014
 */
public class RandGenerator {
	
	/**
	 * random����Java Random���ʵ�������󣬿�ͨ��random����Java
	 * ʵ�ֵĸ�����������ɷ���
	 */
	public static java.util.Random random =new java.util.Random();
	
	/**
	 * rand_equal���������ɲ���min��max֮���һ��double���͵������
	 * @param min ���������������
	 * @param max ���������������
	 * @return min��max֮���һ��double���͵������
	 */
	public static double rand_equal(double min, double max){
		return (random.nextDouble()*(max-min) + min);
	}
	
	/**
	 * rand_peak����������dim������min��max֮���double�����������ƽ��ֵ
	 * @param min ���������������
	 * @param max ���������������
	 * @param dim ��������������Ĵ���
	 * @return dim��min��max֮���double�����������ƽ��ֵ
	 */
	public static double rand_peak(double min, double max, int dim){
		double sum = 0;
		for(int i=0; i<dim; i++){
//			sum += random.nextDouble();		// ������ôд��
			sum += rand_equal(0.0, 1.0);	// 	The Skyline operator����������ôд��
		}
		sum /= dim;
		return (sum*(max-min) + min);
	}

	/**
	 * rand_normal��������������Ϊmed������Ϊvar����̬�ֲ���double���͵������
	 * @param med ������̬�ֲ�������
	 * @param var ������̬�ֲ��ķ���
	 * @return ��������Ϊmed������Ϊvar����̬�ֲ���double���͵������
	 */
	public static double rand_normal(double med, double var, int dim){
//	  return rand_peak(med - var, med + var, dim);	// ԭʼ���룬�����ķ�ʽ���ɵ����ݣ������ͷ���ط���Ԥ�ڣ���������ݲ�����Ԥ��
	  return rand_peak(med - var, med + var, 12);	// ��Skyline Operator�еķ�ʽ�޸ģ��������Ҳ����Ԥ�ڣ�Ϊʲô��12����
	}
	
	/**
	 * rand_normal_unit��������������Ϊmed������Ϊvar������[0.0, 1.0]֮���double���͵������
	 * @param med ������̬�ֲ�������
	 * @param var ������̬�ֲ��ķ���
	 * @return ��������Ϊmed������Ϊvar������[0.0, 1.0]֮���double���͵������
	 */
	public static double rand_normal_unit (double med, double var, int dim){
	  double val = 2.0;
	  while (val <=0 || val >=1){
	      val = rand_normal (med, var, dim);
	  }
	  return val;
	}
	
	/**
	 * is_vector_ok�������double���͵�����x[]��ÿһά�����Ƿ�淶��Ϊ[0.0, 1.0]֮��
	 * @param x �����������double��������
	 * @return ������x[]�Ѿ��淶�����򷵻�true�����򷵻�false
	 */
	public static boolean is_vector_ok(double[] x){
		int dim_temp;
		dim_temp = x.length;

		for(int i=0; i<dim_temp; i++)
		{
			if (x[i] < 0.0 || x[i] > 1.0)
				return false;
		}
		return true;
	}
	
	//**************************************************************************************************
	/**
	 * generate_indep����������һ��ά��Ϊdim����󲻳���DIMENSION���Ҹ�ά���ݶ���
	 * �ֲ�����������ά����ȡֵ��Χ��(0.0, 1.0)֮��
	 * @param dim ָ������������ά�ȣ���󲻳���DIMENSION
	 * @return ��double������ʽ��������ݶ���������x
	 */
	public static double[] generate_indep(int dim){
		double[] x = new double[dim];
		for (int i = 0; i<dim; ++i)
		{
			double rand_value = rand_equal(0, 1);
			x[i] = rand_value;
		}
		return x;
	}
	
	/**
	 * generate_corr����������һ��ά��Ϊdim����󲻳���DIMENSION���Ҹ�ά�������
	 * �ֲ�����������ά����ȡֵ��Χ��(0.0, 1.0)֮��
	 * @param dim ָ������������ά�ȣ���󲻳���DIMENSION
	 * @return ��double������ʽ�����������ص�����x
	 */
	public static double[] generate_corr(int dim)
	{
		boolean isOK = false;
		double[] x = new double[dim];

		while(!isOK){
			double value = rand_peak(0.0, 1.0, dim);
			double temp = (value <= 0.5) ? value : (1.0-value);
			
			for(int i=0; i<dim; i++)
				x[i] = value;
			for(int j=0; j<dim; j++){
				double delta = rand_normal(0, temp, dim);
				x[j] += delta;
				x[(j+1)%dim] -= delta;
			}
			isOK = is_vector_ok(x);
		}

		return x;
	}
	
	
	/**
	 * generate_anti����������һ��ά��Ϊdim����󲻳���DIMENSION���Ҹ�ά���ݷ����
	 * �ֲ�����������ά����ȡֵ��Χ��(0.0, 1.0)֮��
	 * @param dim ָ������������ά�ȣ���󲻳���DIMENSION
	 * @return ��double������ʽ��������ݷ���ص�����x
	 */
	public static double[] generate_anti(int dim)
	{
		boolean isOK = false;
		double[] x = new double[dim];

		while(!isOK){
			double value = rand_normal(0.5, 0.25, dim);
			double temp = (value <= 0.5) ? value : (1.0-value);
			
			for(int i=0; i<dim; i++)
				x[i] = value;
			for(int j=0; j<dim; j++){
//				double delta = rand_normal(0, temp, dim);
				double delta = rand_normal(-1, temp, dim);	// ����Skyline operatorһ���е�д��
				x[j] += delta;
				x[(j+1)%dim] -= delta;
			}
			isOK = is_vector_ok(x);
		}
		return x;
	}
		
	//**************************************************************************************************
	/**
	 * norm_rand����, ����ָ�������ͷ����������̬�ֲ��������
	 * @param miu ��������
	 * @param sigma2 ����ֵ
	 */
	public static double norm_rand(double miu, double sigma2){
		  double N = 12;
		  double x = 0,temp = N;
		  do{
		   x=0;
		   for(int i = 0; i < N; i++)
		    x = x + (Math.random());
		   x = (x - temp/2)/(Math.sqrt(temp/12));
		   x = miu + x*Math.sqrt(sigma2);
		   } while(x <= 0);          //�ڴ��Ұ�С��0�����ų�����
		   return x;
	}
	
	/**
	 * norm_rand����, ����ָ�������ͷ����������̬�ֲ��������,����ֵ�ڷ�Χ[lower, upper)��
	 * @param miu ��������
	 * @param sigma2 ����ֵ
	 * @param lower ��������ֵ������
	 * @param upper ��������ֵ������
	 */
	public static double norm_rand(double miu, double sigma2, double lower, double upper){
		double temp;
		do{
			temp = norm_rand(miu, sigma2);
			//System.out.println(temp);
		}while(temp < lower || temp >= upper);
		return temp;	
	}
	
	/**
	 * norm_rand����, ����ָ�������Ĳ��ɷֲ��������
	 * @param Lamda ����
	 */
	public static double possion_rand(double Lamda){      // ���ɷֲ�
		
		 double x = 0, b = 1, c = Math.exp(-Lamda), u; 
		 do {
		  u = Math.random();
		  b *= u;
		  if(b >= c)
		   x++;
		  } while( b >= c);
		 return x;
	}	
	
	/**
	 * initZipfDist����, ����ָ��������zip�ֲ��������
	 * @param Lamda ����
	 */
	public static float[] initZipfDist(int length) {//zip�ֲ�

		float[] probs = new float[length];
		// initialize the probability with i^(-a)
		float tmp = 0;
		for (int i = 0; i < length; i++) {
			probs[i] = (float) (1.0 / Math.pow(i + 1, 1));
			tmp += probs[i];
		}
		// normalize the probability with C
		// make the sum of probabilities equal 1
		float C = 1 / tmp;
		for (int i = 0; i < length; i++) {
			probs[i] *= C;
		}
		return probs;
	}	
	
	// **********************************************************************
	/**
	 * printArray��������������ÿһά����ֵ
	 * @param x�������������
	 */
	public static void printArray(double[] x){
		for(int i=0; i<x.length; i++){
			System.out.print(x[i]);
			if(i<x.length-1)
				System.out.print(",");
		}	
	}

}
