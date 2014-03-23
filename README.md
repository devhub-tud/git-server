# Git-Server

**Git-Server** is a git server with a REST API and is backed by a local Gitolite installation. The **Git-Server** REST API can be used to manage the local installation of Gitolite. The REST API allows external systems to list, inspect, create or delete users, ssh-keys, groups, and repositories.

## Installation

### Prequisites

We're expecting that you've already installed the following prequisites:

* Gitolite (3.x)
* Java JDK (1.7+)
* Maven (3.1.x)

### Installing `git-server`

* Create a new folder `/etc/git-server`:
```bash
mkdir /etc/git-server
```

* Create another folder `/etc/git-server/mirrors`.
```bash
mkdir /etc/git-server/mirrors
```
	
* Ensure that this folder is owned by the `git` user (same user as used in the Gitolite installation):
```bash
chown -R git:git /etc/git-server
```

* Create a start-stop script in `/etc/init.d/git-server`
```bash
#!/bin/bash

pidFile="/etc/git-server/pid"
startScript="/etc/git-server/start.sh"

start() {
	if [ -f $pidFile ];
	then
		echo "$pidFile already exists. Process is probably still running."
	else
		rm -f $startScript
		touch $startScript
		echo "cd /etc/git-server" >> $startScript
		echo "java -jar git-server.jar >> server.log 2>&1 &" >> $startScript
		echo "echo \$! > $pidFile" >> $startScript
		chmod +x $startScript	

		echo "Starting git-server..."
		su -l git -c $startScript
		rm -f $startScript		

		sleep 1
		echo "Git-server started!"
	fi
}

stop() {
	if [ -f $pidFile ];
	then
		echo "Stopping git-server..."
		pid=`cat ${pidFile}`
		kill $pid
		rm $pidFile

		sleep 1
		echo "Git-server stopped!"
	else
		echo "$pidFile does not exists. Process is probably not running."
	fi
}

build() {
	su git << 'EOF'
	rm -rf /etc/git-server/tmp
	mkdir /etc/git-server/tmp
	cd /etc/git-server/tmp
	git clone https://github.com/devhub-tud/git-server.git
	cd git-server
	mvn clean package -DskipTests=true
EOF
}

deploy() {
	su git << 'EOF'
	cp -rf /etc/git-server/tmp/git-server/git-server/target/git-server-distribution/git-server/. /etc/git-server/
	rm -rf /etc/git-server/tmp
EOF
}

watch() {
	tail -f /etc/git-server/server.log
}

case "$1" in
	start)
		start
		;;
	stop)
		stop
		;;
	status)
		status git-server
		;;
	deploy|redeploy)
		build
		stop
		deploy
		start
		;;
	restart|reload|condrestart)
		stop
		start
		;;
	watch)
		watch
		;;
	*)
		echo $"Usage: $0 {start|stop|restart|reload|status}"
		exit 1

esac
exit 0
```

* Ensure that this file executable:
```bash
chmod +x /etc/init.d/git-server
```

* Deploying git-server:
```bash
/etc/init.d/git-server deploy
```

### Setup mirroring

`Git-server` allows you to query the contents of Git repositories hosted on Gitolite through the REST API. In order to do this it needs to have up-to-date checkouts of all Git repositories. These will be stored in the previously create `/etc/git-server/mirrors` folder. You can set up this synchronization using a Git hook. 

* Create the following file `/home/git/.gitolite/hooks/common/post-receive` with the following contents:
```bash
#!/bin/bash

WORK_DIR="/home/git/mirrors"
REPO_URL="$PWD"

if [ $(git rev-parse --is-bare-repository) = true ]
then
	REPO_NAME=$(basename "$PWD")
	REPO_NAME=${REPO_NAME%.git}
else
	REPO_NAME=$(basename $(readlink -nf "$PWD"/..))
fi

if [ -d "$WORK_DIR/$REPO_NAME" ]; then
	cd "$WORK_DIR/$REPO_NAME"
	unset GIT_DIR
	git pull -q
else
	cd "$WORK_DIR"
	git clone -q "$REPO_URL" "$REPO_NAME"
fi
```

* Ensure that the file is executable
```bash
chmod +x /home/git/.gitolite/hooks/common/post-receive
```

* Tell Gitolite to use the new hook
```bash
/home/git/bin/gitolite setup --hooks-only
```

## I'd like to contribute...

Great! In order to setup a development environment you'll need to have installed the following software:

* Java 7 (or newer)
* Puppet 3
* VirtualBox
* Vagrant

When you have succesfully installed the forementioned software you can run the following commands. These will clone this repository, and boot up a VirtualBox machine and provision it with a Gitolite installation:

```bash
git clone git@github.com:devhub-tud/git-server.git
cd git-server/
vagrant up --provision
```

Now you should have a VM running Gitolite. Next you can import the project into your favorite IDE, and run the `nl.tudelft.ewi.git.GitServer` class to start the server. Please ensure you import the project into your IDE as a Maven project to ensure that all dependencies are automatically downloaded and registered on your build path.

Please not that not all dependencies are present in Maven Central. You'll also need to import the [Java-Gitolite-Manager project](https://github.com/devhub-tud/Java-Gitolite-Manager) into the workspace of your IDE.

### Limitations ###

In order for the `diff`, `tree` and `file` resources to work, you need to create a `/mirrors` folder in the root folder of the project and clone the git repositories you wish to view diffs, trees and files of into this folder. Currently it's not yet possible to automatically share or synchronize this folder with the Gitolite VM to automate this process. In production (where the Git-Server project runs on the same machine as the Gitolite installation) you do not have this problem.
