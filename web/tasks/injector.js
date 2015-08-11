module.exports = {
    options: {
        ignorePath : [
            'src/main/webapp/',
            '../../common/web/src/main/resources/META-INF/resources/',
            '../../intygstyper/fk7263/src/main/resources/META-INF/resources/',
            '../../intygstyper/sjukpenning/src/main/resources/META-INF/resources/'],
        addRootSlash : false
    },
    local_dependencies: {
        files: {
            'src/main/webapp/index.html': [
                'src/main/webapp/js/**/*.js', '!src/main/webapp/js/**/*.min.js',
                'src/main/webapp/vendor/**/*.js', '!src/main/webapp/vendor/**/*.min.js',
                global.paths.common + '/css/**/*.css',
                global.paths.common + '/webcert/css/**/*.css',
                global.paths.common + '/webcert/js/**/*.js',
                global.paths.fk7263 + '/js/**/*.js',
            ]
        }
    }
}
