set :application, 'ld4p-marc21-to-xml'
set :repo_url, 'https://github.com/sul-dlss/ld4p-marc21-to-xml.git'

# Default branch is :master
ask :branch, `git rev-parse --abbrev-ref HEAD`.chomp

# Default deploy_to directory is /var/www/my_app_name
set :deploy_to, "/opt/app/ld4p/ld4p-marc21-to-xml"

# Default value for :format is :airbrussh.
# set :format, :airbrussh

# You can configure the Airbrussh format using :format_options.
# These are the defaults.
# set :format_options, command_output: true, log_file: "log/capistrano.log", color: :auto, truncate: :auto

# Default value for :pty is false
# set :pty, true

# Default value for :linked_files is []
append :linked_files, 'config/config.sh'
append :linked_files, 'xform-marc21-to-xml/src/main/resources/server.conf'

# Default value for linked_dirs is []
append :linked_dirs, 'log'

# Default value for default_env is {}
# set :default_env, { path: "/opt/ruby/bin:$PATH" }

# Default value for keep_releases is 5
# set :keep_releases, 5

# update shared_configs before restarting app
#before 'deploy:restart', 'shared_configs:update'

namespace :maven do
  desc 'package'
  task :package do
    on roles(:app) do
      execute "cd #{current_path} && /usr/local/maven/bin/mvn clean package"
    end
  end
end
after 'deploy:finished', 'maven:package'

namespace :deploy do
  # needs to be in deploy namespace so deploy_host is defined properly (part of current_path)
  desc 'convert test file of one record'
  task :run_test do
    on roles(:app) do
      execute "cd #{current_path} && bin/marc21_to_marcxml_test.sh one_record.mrc"
    end
  end
end
