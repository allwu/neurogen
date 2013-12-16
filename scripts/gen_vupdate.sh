#!/bin/bash

FILE_TEMPLATE=template_vupdate.c

CIRCUIT_NAME=onering
POP_NUM=2
POP=("exc" "inh")
POP_SIZE=ringsize

SYN_exc=("extexc" "inhexc")
SYN_inh=("inhinh" "excinh")

SPK_TYPE=uint128

if [ ! -f $FILE_TEMPLATE ]; then
	echo "template file not found"
	exit -1
fi

for pop in "${POP[@]}"; do
	
	output_filename=$CIRCUIT_NAME"_"$pop"_vupdate.c"
	rm -rf $output_filename

	while read line 
	do		
		var_syn=SYN_${pop};
		eval syn_arr=\( \${${var_syn}[@]} \);

		if [[ "$line" == *"PARA_SYNAPSE"* ]]; then
			for syn_type in "${syn_arr[@]}"; do
				newline="unsigned short int g_${syn_type},"
				echo $newline >> $output_filename
			done
		elif [[ "$line" == *"CURRENT_COMPUTE"* ]]; then
			echo "/* this is the suggested code" >> $output_filename
			for syn_type in "${syn_arr[@]}"; do
				newline="unsigned short int drive_${syn_type} = 0;"
				echo $newline >> $output_filename
			done
			newline="i_pos = "
			for syn_type in "${syn_arr[@]}"; do
				newline=$newline"drive_${syn_type}*g_${syn_type} +"
			done
			echo $newline";" >> $output_filename
			newline="i_neg = "
			for syn_type in "${syn_arr[@]}"; do
				newline=$newline"drive_${syn_type}*g_${syn_type} +"
			done
			echo $newline";" >> $output_filename
			echo "*/" >> $output_filename
		else
			newline=$line
			newline=`echo $newline | sed -e 's/\/\*POPULATION_SIZE\*\//'$POP_SIZE'/g'`
			newline=`echo $newline | sed -e 's/\/\*POPULATION_NAME\*\//'$pop'/g'`
			newline=`echo $newline | sed -e 's/\/\*CIRCUIT_NAME\*\//'$CIRCUIT_NAME'/g' `
			newline=`echo $newline | sed -e 's/\/\*OUT_SPK_TYPE\*\//'$SPK_TYPE'/g' `
			echo $newline >> $output_filename
		fi
	done < $FILE_TEMPLATE
done
