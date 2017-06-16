#!/bin/bash

input_directory="parsed-logs/*"
output_file_c="commits.csv"
output_file_m="merges.csv"
count_c=0
count_m=0

if [ -f $output_file_c ]; then
    rm $output_file_c
    echo "Commits CSV deleted."
fi

if [ -f $output_file_m ]; then
    rm $output_file_m
    echo "Merges CSV deleted."
fi

for file in $input_directory
do
	if [[ ${file: -11} == "commits.csv" ]]
	then
		if [[ $count_c -eq 0 ]]
		then
			cp $file $output_file_c
		else
			tail -n +2 $file >> "$output_file_c"
		fi
		echo "Processed commits CSV: $file"
		count_c=$((count_c+1))
	elif [[ ${file: -10} == "merges.csv" ]]
	then
		if [[ $count_m -eq 0 ]]
		then
			cp $file $output_file_m
		else
			tail -n +2 $file >> "$output_file_m"
		fi
		echo "Processed merges CSV: $file"
		count_m=$((count_m+1))
	fi
done

echo "$count_c commit CSVs and $count_m merge CSVs processed."

# http://stackoverflow.com/a/3746964
echo "Commits CSV has `wc -l $output_file_c | awk {'print $1'}` rows."
echo "Merges CSV has `wc -l $output_file_m | awk {'print $1'}` rows."

echo "Compressing CSV files..."
gzip -k "$output_file_c"
gzip -k "$output_file_m"
echo "CSV files compressed."


