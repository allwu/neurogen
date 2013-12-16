#include "onering_common.h"

#define POPULATION_SIZE	

void exc_vupdate(
		
	volatile uint128* spkout,
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
	uint128 l_spkout = 0;

	for (i=0; i<120; i++) {
#pragma HLS pipeline
		unsigned short int l_vm = vm[i];
		unsigned short int l_rp = rp[i];

		unsigned short int i_pos = 0;	
		unsigned short int i_neg = 0;	

		

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

			l_spkout |= (uint128)1<<i;
		}
	}
	*spkout = l_spkout;
	*spknum = l_spknum;
}

