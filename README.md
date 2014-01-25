# Git-Server

**Git-Server** is a git server with a REST API and is backed by a local Gitolite installation. The **Git-Server** REST API can be used to manage the local installation of Gitolite. The REST API allows external systems to list, inspect, create or delete users, ssh-keys, groups, and repositories.

## I'd like to contribute...

Great! In order to setup a development environment you'll need to have installed the following software:

* Java 7 (or newer)
* Puppet 3
* VirtualBox
* Vagrant

When you have succesfully installed the forementioned software you can run the following commands. These will clone this repository, and boot up a VirtualBox machine and provision it with a Gitolite installation:

```bash
git clone git@github.com:michaeldejong/git-server.git
cd git-server/
vagrant up --provision
```

Now you should have a VM running Gitolite. Next you can import the project into your favorite IDE, and run the `nl.tudelft.ewi.git.GitServer` class to start the server. Please ensure you import the project into your IDE as a Maven project to ensure that all dependencies are automatically downloaded and registered on your build path.
