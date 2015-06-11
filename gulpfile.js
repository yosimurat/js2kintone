var gulp = require('gulp'),
	concat= require('gulp-concat'),
	uglify = require('gulp-uglify'),
	browserify = require('browserify'),
	source = require('vinyl-source-stream'),
	runSequence = require('run-sequence'),
	browser = require('browser-sync'),
	connect = require('gulp-connect');

gulp.task('browserify', function() {
	return browserify({
		entries: ['./js/script.js']
	}).bundle().pipe(source('main.js')).pipe(gulp.dest("./dist/browserify/"));
});


gulp.task('libcombin', function() {
	var libs = [
		'./bower_components/jquery/dist/jquery.min.js',
		'./bower_components/jsrender/jsrender.min.js'
	];
	gulp.src(libs)
		.pipe(concat('bower_components.js'))
		.pipe(gulp.dest('./dist/browserify/'));

});

gulp.task('uglify', function() {
	gulp.src(['./dist/browserify/*.js'])
		.pipe(uglify())
		.pipe(concat('js2kintone.min.js'))
		.pipe(gulp.dest('./dist/js/'))
		.pipe(browser.reload({stream: true}));
});

gulp.task('build', function(callback) {
  return runSequence(
    ['browserify', 'libcombin'],
    'uglify',
    callback
  );
});

gulp.task('server', function() {
	browser({
		baserDir: './',
		directory: true
	});
});

gulp.task('connect', function() {
  connect.server({
    root: './',
    livereload: true
  });
});

gulp.task('default', ['server', 'connect', 'build'], function() {
	gulp.watch(['js/*.js', '*.html'], ['build']);
});

