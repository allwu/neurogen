 
#include "onering_common.h"

#define POPULATION_SIZE	120

void inh_gupdate(
		
	const unsigned short int spknum_inhinh,
const unsigned short int* spkcell_inhinh,
const unsigned short int* wt_inhinh,
const unsigned short int spknum_excinh,
const unsigned short int* spkcell_excinh,
const unsigned short int* wt_excinh,

	unsigned short int* g_inhinh,
unsigned short int* g_excinh,
unsigned short int* g_nmda_excinh,

	int place_holder
	)
{
	int i, k;

	for (i=0; i<POPULATION_SIZE; i++) {
#pragma HLS pipeline
#pragma HLS dependence array intra false variable=g_inhinh
#pragma HLS dependence array intra false variable=g_excinh
#pragma HLS dependence array intra false variable=g_nmda_excinh


		g_inhinh -= g_inhinh[i]>>tau_gaba;
g_excinh -= g_excinh[i]>>tau_nmda;
g_nmda_excinh -= g_nmda_excinh[i]>>tau_nmda;


	}
	for (k=0; k<spknum_inhinh; k++) {
#pragma HLS loop_tripcount min=1 max=1 avg=1
		int j = spkcell_inhinh[k];
		for (i=0; i<POPULATION_SIZE; i++) {
#pragma HLS pipeline
			g_inhinh[i] += 4096 / wt_inhinh[120+i-j];

		}
	}
	for (k=0; k<spknum_excinh; k++) {
#pragma HLS loop_tripcount min=1 max=1 avg=1
		int j = spkcell_excinh[k];
		for (i=0; i<POPULATION_SIZE; i++) {
#pragma HLS pipeline
			g_excinh[i] += (65535 - g_nmda_excinh[j]) / wt_excinh[120+i-j];

		}
	}
}

