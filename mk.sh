#!/usr/bin/env bash
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
START_DATE=$(date +"%T")
pushd "$SCRIPT_DIR"
trap 'popd' EXIT
case $1 in
(help)
	echo <<USAGE
help - print this message
npm - push to web client and run the rest of the command line with npm (e.g. 'run build', or 'start &')
* - run sbt with the rest of the command line
USAGE
	exit 1
;;
(npm)
	shift
	pushd "client/web"
	npm $@
	popd
	;;
(*)
	# punt to sbt
	sbt $@
	;;
esac
echo "--- mk @ $(date +"%T")"
