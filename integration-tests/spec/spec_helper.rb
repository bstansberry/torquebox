require 'torquespec'
require 'capybara/dsl'
require 'akephalos'
require 'fileutils'

TorqueSpec.knob_root = File.expand_path( File.join( File.dirname( __FILE__ ), '..', 'target', 'test-classes' ) )
TorqueSpec.jboss_home = File.expand_path( File.join( File.dirname( __FILE__ ), '..', 'target', 'integ-dist', 'jboss' ) )
TorqueSpec.max_heap = java.lang::System.getProperty( 'max.heap' )
TorqueSpec.lazy = java.lang::System.getProperty( 'jboss.lazy' ) == "true"

Capybara.register_driver :akephalos do |app|
  Capybara::Driver::Akephalos.new(app, :browser => :firefox_3)
end

Capybara.default_driver = :akephalos
Capybara.app_host = "http://localhost:8080"
Capybara.run_server = false

RSpec.configure do |config|
  config.include Capybara
  config.after do
    Capybara.reset_sessions!
  end
end

MUTABLE_APP_BASE_PATH  = File.join( File.dirname( __FILE__ ), '..', 'target', 'apps' )
TESTING_ON_WINDOWS = ( java.lang::System.getProperty( "os.name" ) =~ /windows/i )

def mutable_app(path)
  full_path = File.join( MUTABLE_APP_BASE_PATH, path )
  dest_path = File.dirname( full_path )
  FileUtils.rm_rf( full_path )
  FileUtils.mkdir_p( dest_path )
  FileUtils.cp_r( File.join( File.dirname( __FILE__ ), '..', 'apps', path ), dest_path )
end

def add_request_header(key, value)
  page.driver.browser.send(:client).add_request_header(key, value)
end

