#!/bin/bash

log_directory="logs"
branch_directory="branches"
output_file="branches.csv"
count=0

if [ -f $output_file ]; then
    rm $output_file
    echo "Branches CSV deleted."
fi

# move branch csvs out of log directory
mv "$log_directory/"*"_branches.csv" "$branch_directory/"

# merge branch csvs
for file in "$branch_directory/"*
do
	if [[ ${file: -12} == "branches.csv" ]]
	then
		if [[ $count -eq 0 ]]
		then
			cp $file $output_file
		else
			tail -n +2 $file >> "$output_file"
		fi
		echo "Processed branch CSV: $file"
		count=$((count+1))
	fi
done

echo "$count branch CSVs processed."

# http://stackoverflow.com/a/3746964
echo "Branches CSV has `wc -l $output_file | awk {'print $1'}` rows."

echo "Compressing branches CSV..."
gzip -k "$output_file"
echo "Branches CSV compressed."


