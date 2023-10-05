[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.devngho/kirok-svelte-binding/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.devngho/kirok-svelte-binding)

# kirok-svelte-binding

![kirok](https://kirok.nghodev.com/favicon.png)

✅ [kirok](https://github.com/devngho/kirok) 공식 [Svelte](https://svelte.dev) 바인딩

```kts
kotlin {
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation("io.github.devngho:kirok-svelte-binding-jvm:[VERSION]")
            }
        }
    }
}

kirok {
    binding = listOf("io.github.devngho.kiroksvelte.KirokSvelteBinding")
}
```

```html

<script>
    import {useSimple} from "$lib/Simple";

    const [simpleCounter, {increment}] = useSimple()
    !;
    /* 
    타입: [Writable<{
        count: number;
    }>, {
        increment: () => Promise<void>;
        decrement: () => Promise<void>;
    }] | null
    */
</script>
```

## 지원하는 기능

- [x] kirok의 모든 기능
- [x] isAvailable 함수로 환경 확인
- [x] TypeScript 타이핑
    - [x] 기본 타입, 데이터 클래스
    - [ ] 제네릭