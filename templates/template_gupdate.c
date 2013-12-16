 
#include "/*CIRCUIT_NAME*/_common.h"

#define POPULATION_SIZE	/*POPULATION_SIZE*/

void /*POPULATION_NAME*/_gupdate(
		
	/*INPUT_PARA*/
	/*OUTPUT_PARA*/
	int place_holder
	)
{
	int i, k;

	for (i=0; i<POPULATION_SIZE; i++) {
#pragma HLS pipeline
/*HLS_PRAGMA_DEPEND*/

		/*G_UPDATE*/

	}
	/*BEGIN_REPEAT_SPK*/
	for (k=0; k</*SPK_NUM_VAR*/; k++) {
#pragma HLS loop_tripcount min=1 max=1 avg=1
		int j = /*SPK_CELL_VAR*/[k];
		for (i=0; i<POPULATION_SIZE; i++) {
#pragma HLS pipeline
			/*SPK_UPDATE*/
		}
	}
	/*END_REPEAT_SPK*/
}
