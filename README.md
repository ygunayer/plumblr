# plumblr
A command-line utility that allows you to bulk-download all media from a given tumblr blog.

**Warning:** This project is barely a work in progress, so it's not guaranteed to work perfectly.

## Usage
Specify the blog name as the first command line parameter and run the app using SBT or `java -jar`

```
$ java -jar plumblr (blog name)
```

## TODO
- Allow setting the following options:
  - Target folder
  - Parallelism/throttling
  - Filename pattern
  - Types of media to download
  - Conflict resolution mode (overwrite/ignore)
- Handle more kinds of media (Instagram, Pinterest, etc.)
- Set the last modified dates of output files
- Fix the jumpy progress bar somehow
- Add more tests
- Output streams may not be closed properly (?)

## License
MIT
