exec { "apt-get update":
	path => "/usr/bin",
}

include gitolite::install

class gitolite::install {
	package { "git":
		ensure		=> present,
	}
	package { "perl":
		ensure		=> present,
	}
	package { "openssh-server":
		ensure		=> present,
	}
	
	group { "git":
		ensure		=> present,
	}
	
	user { "git":
		ensure 		=> present,
		home		=> "/home/git",
		password	=> "git",
		managehome	=> true,
		gid			=> "git",
		require		=> [ 
			Group["git"],
		],
	}
	
	file { "/home/git":
		ensure		=> directory,
		owner		=> "git",
		group		=> "git",
		require		=> User["git"],
	}
	
	file { "/home/git/.ssh":
		ensure		=> directory,
		owner		=> "git",
		require		=> File["/home/git"],
	}
	
	file { "/home/git/git.pub":
		owner 		=> "git",
		group 		=> "git",
		mode 		=> 644,
		source		=> "/shared/id_rsa.pub",
		require		=> File["/home/git"],
	}
	
	exec { "clone gitolite":
		command		=> "git clone git://github.com/sitaramc/gitolite.git",
		path		=> ["/usr/local/sbin", "/usr/local/bin", "/usr/sbin", "/usr/bin", "/sbin", "/bin"],
		timeout		=> 0,
		cwd			=> "/home/git",
		user		=> "git",
		group		=> "git",
	}
	
	file { "/home/git/bin":
		ensure		=> directory,
		owner		=> "git",
		require		=> [
			User["git"],
			Exec["clone gitolite"]
		],
	}
	
	exec { "install gitolite":
		command		=> "/home/git/gitolite/install -to /home/git/bin",
		path		=> ["/usr/local/sbin", "/usr/local/bin", "/usr/sbin", "/usr/bin", "/sbin", "/bin"],
		cwd			=> "/home/git",
		user		=> "git",
		group		=> "git",
		require		=> File["/home/git/bin"],
	}
	
	exec { "setup gitolite":
		command		=> "/home/git/bin/gitolite setup -pk git.pub",
		path		=> ["/usr/local/sbin", "/usr/local/bin", "/usr/sbin", "/usr/bin", "/sbin", "/bin"],
		environment	=> "HOME=/home/git",
		cwd			=> "/home/git",
		user		=> "git",
		group		=> "git",
		require		=> Exec["install gitolite"],
	}
	
}