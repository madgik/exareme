#!/usr/bin/env bash

n=0
eof=0

while IFS= read -r line || [[ -n "$line" ]]; do
    n=$[${n}+1]                                 #calculate number of lines so workerN will be written below [workers] tag
    if [[ "$line" == *"[workers]"* ]]; then
        while IFS= read -r line1 || [[ -n "$line1" ]]; do
            if [[ -z "$line1" ]]; then
                continue                        #If empty line do not calculate number of lines.continue..
            fi
            n=$[$n+1]
            worker=$(echo "$line1")
            if [[ "$line1" == *"["* ]]; then    #file reached [workerN] tag
                sed -i ${n}'i'${workerName} ../hosts.ini    #write workerN below [workers] tag
                eof=1
                break
            else
                :
            fi
        done
    fi
done < ../hosts.ini

# Ιf eof reached
if [[ ${eof} != "1" ]]; then                #[workers] tag exist and eof right after tag
    echo -e "" >> ../hosts.ini            #this is needed because sed can not write at eof
    n=$[$n+1]
    sed -i ${n}'i'${workerName} ../hosts.ini    #write workerN below [workers] tag
    flag=1
fi