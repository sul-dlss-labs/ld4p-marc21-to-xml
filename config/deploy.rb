set :application, 'ld4p-marc21-to-xml'
set :repo_url, 'https://github.com/sul-dlss/ld4p-marc21-to-xml.git'
set :deploy_host, "sul-ld4p-converter-#{fetch(:stage)}.stanford.edu"
set :user, 'ld4p'

# Default branch is :master
ask :branch, `git rev-parse --abbrev-ref HEAD`.chomp

# Default deploy_to directory is /var/www/my_app_name
set :deploy_to, "/opt/app/#{fetch(:user)}/#{fetch(:application)}"

server fetch(:deploy_host), user: fetch(:user), roles: 'app'

# allow ssh to host
Capistrano::OneTimeKey.generate_one_time_key!

# Default value for :format is :airbrussh.
# set :format, :airbrussh

# You can configure the Airbrussh format using :format_options.
# These are the defaults.
# set :format_options, command_output: true, log_file: "log/capistrano.log", color: :auto, truncate: :auto

# Default value for :pty is false
# set :pty, true

# Default value for :linked_files is []
#append :linked_files, 'config/settings.yml'

# Default value for linked_dirs is []
append :linked_dirs, 'data', 'log'

# Default value for default_env is {}
# set :default_env, { path: "/opt/ruby/bin:$PATH" }

# Default value for keep_releases is 5
# set :keep_releases, 5
