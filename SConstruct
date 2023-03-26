env = DefaultEnvironment()

env['JARCHDIR'] = 'classes'
#env['JARFLAGS'] = 'cf'

Java('classes', 'GaFr', JAVACFLAGS=['-g'])
Java('classes', source='dyn4j/src/main/java', JAVACFLAGS=['-g'])
Jar(target='GaFr.jar', source='classes')
