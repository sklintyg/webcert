/**
 * Created by stephenwhite on 27/03/15.
 */
module.exports = {
    webcert: {
        cwd: 'src/main/webapp/js/',
        src: ['../views/**/*.html'],
        dest: 'src/main/webapp/js/templates.js',
        options: {
            module: 'webcert',
            url: function(url) {
                return url.replace('../', '/');
            }
        }
    }
};

