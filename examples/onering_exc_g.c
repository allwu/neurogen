 
#include "onering_common.h"

#define POPULATION_SIZE	120

void exc_gupdate(
		
	const unsigned short int spknum_inhexc,
const unsigned short int* spkcell_inhexc,
const unsigned short int* wt_inhexc,
const unsigned short int P_ext,
unsigned long long* randin_ext,

	unsigned short int* g_inhexc,
unsigned short int* g_ext,

	int place_holder
	)
{
	int i, k;

	for (i=0; i<POPULATION_SIZE; i++) {
#pragma HLS pipeline
#pragma HLS dependence array intra false variable=g_inhexc
#pragma HLS dependence array intra false variable=g_ext
#pragma HLS dependence array intra false variable=randin_ext


		g_inhexc -= g_inhexc[i]>>tau_gaba;
randin_ext[i] = randin_ext[i] * 1103515245 + 12345;
unsigned int rk_ext = (randin_ext[i]/65536u)&((unsigned long)8191);
if (rk_ext < P_ext) 
g_ext[i] -= (g_ext[i] >> tau_ampa) - 4096;
else 
g_ext[i] -= (g_ext[i] >> tau_ampa);


	}
	for (k=0; k<spknum_inhexc; k++) {
#pragma HLS loop_tripcount min=1 max=1 avg=1
		int j = spkcell_inhexc[k];
		for (i=0; i<POPULATION_SIZE; i++) {
#pragma HLS pipeline
			g_inhexc[i] += 4096 / wt_inhexc[120+i-j];

		}
	}
}

