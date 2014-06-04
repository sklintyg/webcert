({
    baseUrl: '../webapp/js',
    dir: '../../../target/requirebuild',
    skipDirOptimize: true,
    keepBuildDir: true,
    mainConfigFile: '../webapp/js/main.js',
    map: {
        '*': {
            'webjars': 'webjars'
        }
    },
    modules: [
        {
            name: 'main.min',
            create: true,
            include: [
                'main',
                'webjars/common/webcert/js/directives',
                'webjars/common/webcert/js/filters',
                'webjars/common/webcert/js/messages',
                'webjars/common/webcert/js/services'
            ]
        }
    ],
    optimize: 'uglify2',
    uglify2: {
        mangle: false
    },
    paths: {

        webjars: 'webjars',

        angular: 'webjars/angularjs/1.2.14/angular',
        angularCookies: 'webjars/angularjs/1.2.14/angular-cookies',
        angularRoute: 'webjars/angularjs/1.2.14/angular-route.min',
        angularSanitize: 'webjars/angularjs/1.2.14/angular-sanitize.min',
        angularSwedish: 'webjars/angularjs/1.2.14/i18n/angular-locale_sv-se',
        angularUiBootstrap: 'webjars/angular-ui-bootstrap/0.10.0/ui-bootstrap-tpls',

        text: 'webjars/requirejs-text/2.0.10/text'
    },
    wrapShim: true,
    onBuildWrite: function (moduleName, path, contents) {
        return contents.replace(/\/web\/webjars/g, 'webjars');
    }
})
