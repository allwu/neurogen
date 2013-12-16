#include "/*CIRCUIT_NAME*/_common.h"

#define POPULATION_SIZE	

void /*POPULATION_NAME*/_vupdate(
		
	volatile /*OUT_SPK_TYPE*/* spkout,
	/*PARA_SYNAPSE*/
	/*PARA_SPK*/
	//volatile unsigned short char* spknum,
	//unsigned short char* spkcell,
	unsigned short int* vm,
	unsigned short int* rp
	)
{

	int i;

	unsigned char l_spknum = 0;
	/*OUT_SPK_TYPE*/ l_spkout = 0;

	for (i=0; i</*POPULATION_SIZE*/; i++) {
#pragma HLS pipeline
		unsigned short int l_vm = vm[i];
		unsigned short int l_rp = rp[i];

		unsigned short int i_pos = 0;	
		unsigned short int i_neg = 0;	

		/*CURRENT_COMPUTE*/

		if (l_rp > 0) {
			l_vm = iafreset_exc; 
			l_rp -= 1;            
		}
		else {
			if (l_vm > i_neg - i_pos)
				l_vm += i_pos - i_neg;
			else 
				l_vm += i_pos;  
		}
		
		if (l_vm >= iafthresh_exc) {  
			l_rp_exc = rsteps_exc; 

			spkcells[l_spknum] = i;
			l_spknum += 1;

			l_spkout |= (/*OUT_SPK_TYPE*/)1<<i;
		}
	}
	*spkout = l_spkout;
	*spknum = l_spknum;
}
