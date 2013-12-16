#include "ap_cint.h"
#include "ap_interfaces.h"

#include "/*CIRCUIT_NAME*/_common.h"

void gupdate_top(
		/*GUPDATE_PARA*/
		int place_holder
		) {
	/*GUPDATE_CALLS*/
}

void vupdate_top(
		/*VUPDATE_PARA*/
		int place_holder
		) {
	/*VUPDATE_CALLS*/
}


void /*CIRCUIT_NAME*/_top(
		int nsteps,
		int mode,
		volatile unsigned int* input_bus,
		volatile unsigned int* output_bus,
		/*INPUT_VARS*/
		/*OUTPUT_VARS*/
		) 
{
#pragma HLS INTERFACE ap_ctrl_hs port=return
AP_CONTROL_BUS_AXI(BUS_A);
AP_BUS_AXI4_MASTER(input_bus);
AP_BUS_AXI4_MASTER(output_bus);
AP_INTERFACE_REG_AXI4_LITE(nsteps, BUS_A, ap_none);
AP_INTERFACE_REG_AXI4_LITE(mode, BUS_A, ap_none);
/*AXI_INTERFACE_INPUT*/
/*AXI_INTERFACE_OUTPUT*/

	/*DECL_VAR*/
	
	// output_buffers
	const int out_spk_size = 4;	// in dword
	const int out_delay = 8;

	/*DECL_OUT_BUF*/
	// unsigned int buf_spk_inh[buf_size_byte*8];
	// unsigned int l_spk_inh[buf_size];
	
	int i;
	int t;

	if (mode==0) {	// load parameters

		// list populations
		/*INIT_VARS*/
	}
	else {			// start simulation
		
		for (t=0; t<nsteps; t++) {

			/*GTOP_CALLS*/
			/*VTOP_CALLS*/
		
			/*OUTPUT_DECODE*/
			// buf_spk_exc[(t % 8)<<2 + 0] = l_spk_exc>>96;
			// buf_spk_exc[(t % 8)<<2 + 1] = l_spk_exc>>64;
			// buf_spk_exc[(t % 8)<<2 + 2] = l_spk_exc>>32;
			// buf_spk_exc[(t % 8)<<2 + 3] = l_spk_exc;

			// buf_spk_inh[(t % 8)<<2 + 0] = l_spk_inh>>96;
			// buf_spk_inh[(t % 8)<<2 + 1] = l_spk_inh>>64;
			// buf_spk_inh[(t % 8)<<2 + 2] = l_spk_inh>>32;
			// buf_spk_inh[(t % 8)<<2 + 3] = l_spk_inh;

			if (t % out_delay == out_delay-1) {
				/*OUTPUT_MEMCPY*/
				//memcpy(out_spk_inh+t*32, buf_spk_inh, 32*sizeof(int));
			}
	}
}


