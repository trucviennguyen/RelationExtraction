/************************************************************************/
/*                                                                      */
/*   kernel.h                                                           */
/*                                                                      */
/*   User defined kernel function. Feel free to plug in your own.       */
/*                                                                      */
/*   Copyright: Alessandro Moschitti                                    */
/*   Date: 20.11.06                                                     */
/*                                                                      */
/************************************************************************/

/* KERNEL_PARM is defined in svm_common.h The field 'custom' is reserved for */
/* parameters of the user defined kernel. */
/* Here is an example of custom kernel on a forest and vectors*/                          

/*
struct tree_kernel_parameters{
			short kernel_type;
			short TKGENERALITY;
			double lambda;
			double mu;
			double weight;
			short normalization;
}

#define ACL07_KERNEL		-1	// Moschtti et al. 2007
#define SUBTREE_KERNEL		0  // VISHTANAM ans SMOLA 2002
#define SUBSET_TREE_KERNEL	1  // COLLINS and DUFFY 2002
#define BOW_SST_KERNEL		2 // ZHANG, 2003
#define PARTIAL_TREE_KERNEL	3 // Moschitti, ECML 2006
#define NOLEAVES_PT_KERNEL	4 //
#define STRING_KERNEL		6 // Taylor and Cristianini book 2004
#define NOKERNEL           -10 // don't use this tree in any tree kernel

*/

TKP tree_kernel_params[MAX_NUMBER_OF_TREES];/*={
//									QUESTION

//   PT  			  BOW				POS
{ACL07_KERNEL,1,.4,.4,1,1}, {ACL07_KERNEL,1,.4,.4,1,1}, {NOKERNEL,1,.4,1,1,1},{NOKERNEL,1,.4,1,1,1},{NOKERNEL,1,.4,1,1,1},	// 0-4

// PTs
{NOKERNEL,1,.4,1,1,1},		{NOKERNEL,1,.4,1,1,1},		{NOKERNEL,1,.4,1,1,1},{NOKERNEL,1,.4,1,1,1},{NOKERNEL,1,.4,1,1,1},		// 5-9

// PAS
{NOKERNEL,1,.4,1,1,1},		{NOKERNEL,1,.4,1,1,1},		{NOKERNEL,1,.4,1,1,1},{NOKERNEL,1,.4,1,1,1},{NOKERNEL,1,.4,1,1,1},		// 10-14

//									ANSWER

//    PT			 BOW            POS			  
{ACL07_KERNEL,1,.4,1,1,1}, {ACL07_KERNEL,1,.4,1,1,1},	{NOKERNEL,1,.4,1,1,1},{NOKERNEL,1,.4,1,1,1},{NOKERNEL,1,.4,1,1,1},			// 15-19

//			PAS0					  PAS1							PAS2					PAS3			PAS4
{ACL07_KERNEL,1,.4,1,1,1}, {ACL07_KERNEL,1,.4,1,1,1},	{ACL07_KERNEL,1,.4,1,1,1},{NOKERNEL,1,.4,1,1,1},{NOKERNEL,1,.4,1,1,1},				// 20-24

// END

{END_OF_TREE_KERNELS,0,0,0,0,0}

};
*/

double custom_kernel(KERNEL_PARM *kernel_parm, DOC *a, DOC *b) 
{

  int i,j;
  double k, ct, sk2, sk3, sk4, sk5, sk6, sk7, ek, alpha = 0.2;

  i=0;
		kernel_parm->first_kernel=tree_kernel_params[i].kernel_type;
		LAMBDA = tree_kernel_params[i].lambda; 
		LAMBDA2 = LAMBDA*LAMBDA;
		MU=tree_kernel_params[i].mu;
		TKGENERALITY=tree_kernel_params[i].TKGENERALITY;

        ct = tree_kernel_params[i].weight*tree_kernel(kernel_parm, a, b, i, i); 

  i=2;
		kernel_parm->first_kernel=tree_kernel_params[i].kernel_type;
		LAMBDA = tree_kernel_params[i].lambda; 
		LAMBDA2 = LAMBDA*LAMBDA;
		MU=tree_kernel_params[i].mu;
		TKGENERALITY=tree_kernel_params[i].TKGENERALITY;

        sk2 = tree_kernel_params[i].weight*tree_kernel(kernel_parm, a, b, i, i); 

  i=3;
		kernel_parm->first_kernel=tree_kernel_params[i].kernel_type;
		LAMBDA = tree_kernel_params[i].lambda; 
		LAMBDA2 = LAMBDA*LAMBDA;
		MU=tree_kernel_params[i].mu;
		TKGENERALITY=tree_kernel_params[i].TKGENERALITY;

        sk3 = tree_kernel_params[i].weight*tree_kernel(kernel_parm, a, b, i, i); 

  i=4;
		kernel_parm->first_kernel=tree_kernel_params[i].kernel_type;
		LAMBDA = tree_kernel_params[i].lambda; 
		LAMBDA2 = LAMBDA*LAMBDA;
		MU=tree_kernel_params[i].mu;
		TKGENERALITY=tree_kernel_params[i].TKGENERALITY;

        sk4 = tree_kernel_params[i].weight*tree_kernel(kernel_parm, a, b, i, i); 

  i=5;
		kernel_parm->first_kernel=tree_kernel_params[i].kernel_type;
		LAMBDA = tree_kernel_params[i].lambda; 
		LAMBDA2 = LAMBDA*LAMBDA;
		MU=tree_kernel_params[i].mu;
		TKGENERALITY=tree_kernel_params[i].TKGENERALITY;

        sk5 = tree_kernel_params[i].weight*tree_kernel(kernel_parm, a, b, i, i); 

  i=6;
		kernel_parm->first_kernel=tree_kernel_params[i].kernel_type;
		LAMBDA = tree_kernel_params[i].lambda; 
		LAMBDA2 = LAMBDA*LAMBDA;
		MU=tree_kernel_params[i].mu;
		TKGENERALITY=tree_kernel_params[i].TKGENERALITY;

        sk6 = tree_kernel_params[i].weight*tree_kernel(kernel_parm, a, b, i, i); 

  i=7;
		kernel_parm->first_kernel=tree_kernel_params[i].kernel_type;
		LAMBDA = tree_kernel_params[i].lambda; 
		LAMBDA2 = LAMBDA*LAMBDA;
		MU=tree_kernel_params[i].mu;
		TKGENERALITY=tree_kernel_params[i].TKGENERALITY;

        sk7 = tree_kernel_params[i].weight*tree_kernel(kernel_parm, a, b, i, i); 

  ek = basic_kernel(kernel_parm, a, b, 0, 0)+ basic_kernel(kernel_parm, a, b, 1, 1);

  k = (1-alpha)*(ct + sk2 + sk3 + sk4 + sk5 + sk6 + sk7) + alpha*ek*ek;

  return k;
}
/*
{-1,1,.4,.4,1,1},  {-1,1,.4,.4,1,1},  {-10,1,.4,1,1,1},{-10,1,.4,1,1,1},{-10,1,.4,1,1,1},	// 0-4

// PTs
{-10,1,.4,1,1,1},{-10,1,.4,1,1,1},{-10,1,.4,1,1,1},{-10,1,.4,1,1,1},{-10,1,.4,1,1,1},		// 5-9

// PAS
{-10,1,.4,1,1,1},{-10,1,.4,1,1,1},{-10,1,.4,1,1,1},{-10,1,.4,1,1,1},{-10,1,.4,1,1,1},		// 10-14

//									ANSWER

//    PT			 BOW            POS			  
{-1,1,.4,1,1,1},{-1,1,.4,1,1,1},{-10,1,.4,1,1,1},{-10,1,.4,1,1,1},{-10,1,.4,1,1,1},			// 15-19

//    PAS0			PAS1			PAS2		  PAS3			PAS4
{1,1,.4,1,1,1},{1,1,.4,1,1,1},{1,1,.4,1,1,1},{-10,1,.4,1,1,1},{-10,1,.4,1,1,1},				// 20-24

// END

{END_OF_TREE_KERNELS,0,0,0,0,0}

};*/
