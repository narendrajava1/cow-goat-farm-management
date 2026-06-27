Easy — just recreate the Modelfile with 64K and rebuild:

```bash
cat > ~/Modelfile.qwen3coder << 'EOF'
FROM qwen3-coder:30b
PARAMETER num_ctx 65536
PARAMETER num_predict -1
PARAMETER temperature 0.2
PARAMETER num_gpu 99
EOF

ollama create qwen3-coder-64k -f ~/Modelfile.qwen3coder
```

Then verify it got created:

```bash
ollama list
```

You should see both `qwen3-coder-32k` and `qwen3-coder-64k` in the list.

## Update your Continue config

```bash
nano ~/.continue/config.yaml
```

Change the model name from `qwen3-coder-32k` to `qwen3-coder-64k`:

```yaml
models:
  - name: Qwen3-Coder 30B 64K (Chat)
    provider: ollama
    model: qwen3-coder-64k
    apiBase: http://localhost:11434
    roles:
      - chat
      - edit
      - apply

  - name: Qwen2.5-Coder 1.5B (Autocomplete)
    provider: ollama
    model: qwen2.5-coder:1.5b
    apiBase: http://localhost:11434
    roles:
      - autocomplete
```

## Verify 64K is actually being used

```bash
# While a request is running, check this in another terminal
ollama ps
```

You should see `num_ctx=65536` in the output. If you want to be extra sure:

```bash
OLLAMA_DEBUG=1 ollama serve 2>&1 | grep num_ctx
```

Look for `runner.num_ctx=65536` in the logs — that confirms it's not silently falling back to 4K.

## Will 64K fit in your 32GB RAM?

Yes comfortably. Rough breakdown on your machine:

| Component | RAM usage |
|---|---|
| qwen3-coder:30b model weights | ~18GB |
| 64K context KV cache | ~4-5GB |
| macOS + IntelliJ + Ollama overhead | ~4GB |
| **Total** | **~26-27GB** |

That leaves ~5GB headroom on your 32GB — safe enough. The `num_gpu 99` parameter in the Modelfile tells Ollama to push as many layers as possible to the Metal GPU, which on Apple Silicon means the entire model stays in unified memory and runs fast.

Once it's built, try your Spring Boot task again and it should complete fully without truncating.


```bash
cat > ~/Modelfile.qwen3coder << 'EOF'
FROM qwen3.6:27b
PARAMETER num_ctx 65536
PARAMETER num_predict -1
PARAMETER temperature 0.2
PARAMETER num_gpu 99
EOF

ollama create qwen3.6:27b-coder-64k -f ~/Modelfile.qwen3coder
```

Then verify it got created:

```bash
ollama list
```

You should see both `qwen3-coder-32k` and `qwen3-coder-64k` in the list.

## Update your Continue config

```bash
nano ~/.continue/config.yaml
```

Change the model name from `qwen3-coder-32k` to `qwen3-coder-64k`:

```yaml
models:
  - name: Qwen3-Coder 30B 64K (Chat)
    provider: ollama
    model: qwen3-coder-64k
    apiBase: http://localhost:11434
    roles:
      - chat
      - edit
      - apply

  - name: Qwen2.5-Coder 1.5B (Autocomplete)
    provider: ollama
    model: qwen2.5-coder:1.5b
    apiBase: http://localhost:11434
    roles:
      - autocomplete
  - name: qwen3:14b 32K (Chat)
    provider: ollama
    model: qwen3:14b-32k
    apiBase: http://localhost:11434
    roles:
      - chat
      - edit
      - apply
```

## Verify 64K is actually being used

```bash
# While a request is running, check this in another terminal
ollama ps
```

cat > ~/Modelfile.qwen3coder << 'EOF'
FROM qwen3:14b
PARAMETER num_ctx 65536
PARAMETER num_predict -1
PARAMETER temperature 0.2
PARAMETER num_gpu 99
EOF

ollama create qwen3:14b-32k -f ~/Modelfile.qwen3coder