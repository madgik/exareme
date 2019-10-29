#!/usr/bin/env bash
#TODO what if the user enters # in exareme.yaml. FIX that

# Check if EXAREME image exists
docker_image_exists() {
    curl -s -f https://index.docker.io/v1/repositories/$1/tags/$2 >/dev/null
}

# Update exareme.yaml file
resetExaremeFile () {
	while true
	do
		if [[ -f ../group_vars/exareme.yaml ]]; then
			rm -f ../group_vars/exareme.yaml
		fi
		echo -e "\nType your EXAREME image name:"
		read name

		echo -e "\nType your EXAREME image tag:"
		read tag

		echo -e "\nChecking if EXAREME image: "\"${name}":"${tag}\"" exists"

		# Docker image may exist in docker hub or locally.
		if docker_image_exists ${name} ${tag} || [[ "$(sudo docker images -q ${name}:${tag} 2> /dev/null)" != ""  ]]; then
			echo "EXAREME Image exists. Continuing..."

			#write image:tag only if image:tag exists
			echo "EXAREME_IMAGE:" \"${name}\" >> ../group_vars/exareme.yaml
			echo "EXAREME_TAG:" \"${tag}\" >> ../group_vars/exareme.yaml

			break
		else
			echo -e "\nEXAREME image does not exist! EXAREME image name should have a format like: \"hbpmip/exareme\". And EXAREME image tag should have a format like: \"latest\""
		fi
	done
}



if [[ -f ../group_vars/exareme.yaml ]]; then
    echo -e "\nThe file with the exareme docker image information (\"exareme.yaml\") exists."

    #Read image from file exareme.yaml
    image=""
    while read -r line  ; do
        if [[ ${line:0:1} == "#" ]] || [[ -z ${line} ]] ; then  #comment line or empty line, continue
            continue
        fi

        image=$(echo ${image})$(echo "$line" | cut -d ':' -d ' ' -d '"' -f 2 -d '"')":"

    done < ../group_vars/exareme.yaml

    #remove the last : from string
    image=${image:0:-1}

    #imageName the first half of string image
    name=$(echo "$image" | cut -d ':' -f 1)

    #tag the second half of string image
    tag=$(echo "$image" | cut -d ':' -f 2 )

    echo "EXAREME image that will be used is: "\"${name}":"${tag}\"
    echo "Do you wish to change EXAREME image?[ y/n ]"
    read answer

    while true
    do
        if [[ ${answer} == "y" ]]; then
            resetExaremeFile
            break
        elif [[ ${answer} == "n" ]]; then
            break
        else
            echo "$answer is not a valid answer! Try again.. [ y/n ]"
            read answer
        fi
    done
else
    echo -e "\nCreating file for Exareme docker image information..."
    resetExaremeFile
fi

sleep 1
return