# Shadowhunt package-info.java Generator

This Maven project generates package-info.java for every Java package that does
not already contain such a file. You can generate findbugs, sportbugs,
checkstyle, pmd, ... package annotations for each package and save the trouble
of keeping them in sync.

*Usage*

```xml
...
<plugin>
    <groupId>de.shadowhunt.maven.plugins</groupId>
    <artifactId>package-info-maven-plugin</artifactId>
    <version>2.0.0</version>
    <executions>
        <execution>
            <phase>generate-sources</phase>
            <goals>
                <goal>package-info</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <packages>
            <package regex="net.example.database">
                <annotations>
                    <annotation>@de.shadowhunt.annotation.ReturnValuesAreNonnullByDefault</annotation>
                </annotations>
            </package>
            <package><!-- all other packages -->
                <annotations>
                    <annotation>@de.shadowhunt.annotation.ReturnValuesAreNonnullByDefault</annotation>
                    <annotation>@javax.annotation.ParametersAreNonnullByDefault</annotation>
                </annotations>
            </package>
        </packages>
    </configuration>
</plugin>
...
```
