# **TimedScript Execution**

- Sign Type `timedscript`
- Used to execute a script provided by [TimedScripts](https://www.spigotmc.org/resources/timed-scripts.28121)
- Feature Name: `tmdScr`
    - Subnames: `execution`

## Running a script

1. Make sure to have [TimedScripts](https://www.spigotmc.org/resources/timed-scripts.28121) installed.
2. Create a script at `/plugins/TimedScripts/scripts/<name>.txt`.
3. Make sure it runs correctly using `/script run <name>`
4. Place this sign on your track:

```
[!train]
timedscript
<name>
[variable]
```

Variables are defined exactly as in `/script run` (`name=value`, read in the script with `%name%`), except, due to the
lacking space, the sign only allows for up to one value to be assigned.

All usable `%sender...%` values are also taken from the running sign.