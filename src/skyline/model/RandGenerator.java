package skyline.model;
/**
 * @author Purple Wang
 * Jan 20, 2014
 */
public class RandGenerator {
	
	/**
	 * random对象，Java Random类的实例化对象，可通过random调用Java
	 * 实现的各种随机数生成方法
	 */
	public static java.util.Random random =new java.util.Random();
	
	/**
	 * rand_equal方法，生成参数min和max之间的一个double类型的随机数
	 * @param min 定义随机数的下限
	 * @param max 定义随机数的上限
	 * @return min和max之间的一个double类型的随机数
	 */
	public static double rand_equal(double min, double max){
		return (random.nextDouble()*(max-min) + min);
	}
	
	/**
	 * rand_peak方法，生成dim个参数min和max之间的double类型随机数的平均值
	 * @param min 定义随机数的下限
	 * @param max 定义随机数的上限
	 * @param dim 定义生成随机数的次数
	 * @return dim个min和max之间的double类型随机数的平均值
	 */
	public static double rand_peak(double min, double max, int dim){
		double sum = 0;
		for(int i=0; i<dim; i++){
//			sum += random.nextDouble();		// 我是这么写的
			sum += rand_equal(0.0, 1.0);	// 	The Skyline operator的作者是这么写的
		}
		sum /= dim;
		return (sum*(max-min) + min);
	}

	/**
	 * rand_normal方法，生成期望为med，方差为var的正态分布的double类型的随机数
	 * @param med 定义正态分布的期望
	 * @param var 定义正态分布的方差
	 * @return 服从期望为med，方差为var的正态分布的double类型的随机数
	 */
	public static double rand_normal(double med, double var, int dim){
//	  return rand_peak(med - var, med + var, dim);	// 原始代码，这样的方式生成的数据，独立和反相关符合预期，但相关数据不符合预期
	  return rand_peak(med - var, med + var, 12);	// 按Skyline Operator中的方式修改，相关数据也符合预期（为什么是12？）
	}
	
	/**
	 * rand_normal_unit方法，生成期望为med，方差为var且落在[0.0, 1.0]之间的double类型的随机数
	 * @param med 定义正态分布的期望
	 * @param var 定义正态分布的方差
	 * @return 服从期望为med，方差为var且落在[0.0, 1.0]之间的double类型的随机数
	 */
	public static double rand_normal_unit (double med, double var, int dim){
	  double val = 2.0;
	  while (val <=0 || val >=1){
	      val = rand_normal (med, var, dim);
	  }
	  return val;
	}
	
	/**
	 * is_vector_ok方法检测double类型的向量x[]的每一维数据是否规范化为[0.0, 1.0]之间
	 * @param x 待检测向量，double类型数组
	 * @return 若向量x[]已经规范过，则返回true，否则返回false
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
	 * generate_indep方法，生成一组维度为dim（最大不超过DIMENSION）且各维数据独立
	 * 分布的向量，各维数据取值范围在(0.0, 1.0)之间
	 * @param dim 指定生成向量的维度，最大不超过DIMENSION
	 * @return 以double数组形式保存的数据独立的向量x
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
	 * generate_corr方法，生成一组维度为dim（最大不超过DIMENSION）且各维数据相关
	 * 分布的向量，各维数据取值范围在(0.0, 1.0)之间
	 * @param dim 指定生成向量的维度，最大不超过DIMENSION
	 * @return 以double数组形式保存的数据相关的向量x
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
	 * generate_anti方法，生成一组维度为dim（最大不超过DIMENSION）且各维数据反相关
	 * 分布的向量，各维数据取值范围在(0.0, 1.0)之间
	 * @param dim 指定生成向量的维度，最大不超过DIMENSION
	 * @return 以double数组形式保存的数据反相关的向量x
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
				double delta = rand_normal(-1, temp, dim);	// 这是Skyline operator一文中的写法
				x[j] += delta;
				x[(j+1)%dim] -= delta;
			}
			isOK = is_vector_ok(x);
		}
		return x;
	}
		
	//**************************************************************************************************
	/**
	 * norm_rand方法, 生成指定期望和方差的满足正态分布的随机数
	 * @param miu 期望参数
	 * @param sigma2 方差值
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
		   } while(x <= 0);          //在此我把小于0的数排除掉了
		   return x;
	}
	
	/**
	 * norm_rand方法, 生成指定期望和方差的满足正态分布的随机数,且其值在范围[lower, upper)内
	 * @param miu 期望参数
	 * @param sigma2 方差值
	 * @param lower 产生数据值的下限
	 * @param upper 产生数据值的上限
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
	 * norm_rand方法, 生成指定参数的泊松分布的随机数
	 * @param Lamda 参数
	 */
	public static double possion_rand(double Lamda){      // 泊松分布
		
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
	 * initZipfDist方法, 生成指定参数的zip分布的随机数
	 * @param Lamda 参数
	 */
	public static float[] initZipfDist(int length) {//zip分布

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
	 * printArray方法，输出数组的每一维数据值
	 * @param x，待输出的数组
	 */
	public static void printArray(double[] x){
		for(int i=0; i<x.length; i++){
			System.out.print(x[i]);
			if(i<x.length-1)
				System.out.print(",");
		}	
	}

}
