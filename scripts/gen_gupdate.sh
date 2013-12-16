#!/bin/bash

FILE_TEMPLATE=template_gupdate.c

CIRCUIT_NAME=onering
POP_NUM=2
POP=("exc" "inh")
POP_SIZE=ringsize

SYN_exc=("extexc" "inhexc")
SYN_inh=("inhinh" "excinh")

OTHER_exc=("next_rand")
OTHER_inh=("g_excinh_nmda")

if [ ! -f $FILE_TEMPLATE ]; then
	echo "template file not found"
	exit -1
fi

for pop in "${POP[@]}"
do

	output_filename=$CIRCUIT_NAME"_"$pop"_gupdate.c"
	rm -rf $output_filename

	repeat_start=
	repeat_end=
	repeat_content=
	while read line 
	do		
		var_syn=SYN_${pop};
		eval syn_arr=\( \${${var_syn}[@]} \);
		var_other=OTHER_${pop}
		eval other_arr=\( \${${var_other}[@]} \);

		
		if [[ "$line" == *"INPUT_PARA"* ]]; then
			for syn_type in "${syn_arr[@]}"; do
				newline="volatile unsigned short int spknum_${syn_type},"
				echo $newline >> $output_filename
				newline="unsigned short int* spkcell_${syn_type},"
				echo $newline >> $output_filename
			done
			for syn_type in "${syn_arr[@]}"; do
				newline="const unsigned short int* wt_${syn_type},"
				echo $newline >> $output_filename
			done
		elif [[ "$line" == *"OUTPUT_PARA"* ]]; then
			for syn_type in "${syn_arr[@]}"; do
				newline="unsigned short int* g_${syn_type},"
				echo $newline >> $output_filename
			done
			for other_type in "${other_arr[@]}"; do
				newline="unsigned short int* ${other_type},"
				echo $newline >> $output_filename
			done
		elif [[ "$line" == *"HLS_PRAGMA_DEPEND"* ]]; then
			for syn_type in "${syn_arr[@]}"; do
				newline="#pragma HLS dependence array intra false variable=g_${syn_type}"
				`echo $newline >> $output_filename`
			done
		elif [[ "$line" == *"BEGIN_REPEAT_SPK"* ]]; then
			repeat_spk=1
			repeat_end=
			continue
		elif [[ "$line" == *"END_REPEAT_SPK"* ]]; then
			repeat_spk=
			repeat_end=1
		elif [[ "$line" == *"G_UPDATE"* ]]; then
			for syn_type in "${syn_arr[@]}"; do
				newline="g_${syn_type} -= g_${syn_type} >> tau_ ;"
				`echo $newline >> $output_filename`
			done
		else
		# repeatedly output spiking for-loops
			if [ ! -z $repeat_spk ]; then
				repeat_content=${repeat_content}''${line}'\n'
			else
				if [ ! -z $repeat_end ]; then
					for syn_type in "${syn_arr[@]}"; do
						syn_update="g_${syn_type}[i]+=4096u/wt_${syn_type}[POPULATION_SIZE+i-j];"

						syn_content=$repeat_content
						syn_content=`echo $syn_content | sed -e 's/\/\*SPK_NUM_VAR\*\//spknum_'$syn_type'/g'`
						syn_content=`echo $syn_content | sed -e 's/\/\*SPK_CELL_VAR\*\//spkcell_'$syn_type'/g'`
						echo -e $syn_content | sed 's/\/\*SPK_UPDATE\*\//'$(echo $syn_update | sed -e 's/\\/\\\\/g' -e 's/\//\\\//g' -e 's/&/\\\&/g')'/g' >> $output_filename

					done
					repeat_end=
				fi
				newline=`echo $line | sed -e 's/\/\*POPULATION_SIZE\*\//'$POP_SIZE'/g' | sed -e 's/\/\*POPULATION_NAME\*\//'$CIRCUIT_NAME'_'$pop'/g' | sed -e 's/\/\*CIRCUIT_NAME\*\//'$CIRCUIT_NAME'/g' `
				echo $newline >> $output_filename
			fi
		fi


	done < $FILE_TEMPLATE
done

